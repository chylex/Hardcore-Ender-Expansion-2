package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

abstract class TileEntityBase : TileEntity(){
	protected companion object{
		private const val TAG_NAME = "hee"
		
		@JvmStatic protected val FLAG_NOTIFY_NEIGHBORS = 1
		@JvmStatic protected val FLAG_SYNC_CLIENT      = 2
		@JvmStatic protected val FLAG_SKIP_RENDER      = 4
		@JvmStatic protected val FLAG_RENDER_IMMEDIATE = 8
	}
	
	// Synchronization
	
	protected inner class Notifying<T>(initialValue: T, private val notifyFlags: Int) : ObservableProperty<T>(initialValue){
		override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T){
			if (isLoaded && hasWorld() && !world.isRemote){
				notifyUpdate(notifyFlags)
			}
		}
	}
	
	private var isLoaded = false
	
	final override fun onLoad(){
		isLoaded = true
	}
	
	protected fun notifyUpdate(flags: Int){
		val pos = pos // TODO refactor with BlockPos extensions
		val state = world.getBlockState(pos)
		world.notifyBlockUpdate(pos, state, state, flags)
	}
	
	// NBT
	
	protected enum class Context{
		STORAGE, NETWORK
	}
	
	protected abstract fun writeNBT(nbt: NBTTagCompound, context: Context)
	protected abstract fun readNBT(nbt: NBTTagCompound, context: Context)
	
	// NBT: Storage
	
	final override fun writeToNBT(nbt: NBTTagCompound): NBTTagCompound = super.writeToNBT(nbt).apply {
		val heeTag = getCompoundTag(TAG_NAME)
		writeNBT(heeTag, STORAGE)
		setTag(TAG_NAME, heeTag)
	}
	
	final override fun readFromNBT(nbt: NBTTagCompound){
		super.readFromNBT(nbt)
		readNBT(nbt.getCompoundTag(TAG_NAME), STORAGE)
	}
	
	// NBT: Network load
	
	final override fun getUpdateTag(): NBTTagCompound = super.getUpdateTag().apply {
		val heeTag = getCompoundTag(TAG_NAME)
		writeNBT(heeTag, NETWORK)
		setTag(TAG_NAME, heeTag)
	}
	
	final override fun handleUpdateTag(nbt: NBTTagCompound){
		super.handleUpdateTag(nbt)
		readNBT(nbt.getCompoundTag(TAG_NAME), NETWORK)
	}
	
	// NBT: Network update
	
	override fun getUpdatePacket(): SPacketUpdateTileEntity{
		return SPacketUpdateTileEntity(pos, 0, NBTTagCompound().apply { writeNBT(this, NETWORK) })
	}
	
	override fun onDataPacket(net: NetworkManager, packet: SPacketUpdateTileEntity){
		readNBT(packet.nbtCompound, NETWORK)
	}
}
