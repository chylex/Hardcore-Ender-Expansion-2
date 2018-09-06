package chylex.hee.game.block.entity
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.system.util.getState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

abstract class TileEntityBase : TileEntity(){
	protected open fun firstTick(){}
	
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
		firstTick()
	}
	
	protected fun notifyUpdate(flags: Int){
		val pos = pos
		val state = pos.getState(world)
		world.notifyBlockUpdate(pos, state, state, flags)
	}
	
	// NBT
	
	private companion object{
		const val TAG_NAME = HardcoreEnderExpansion.ID
	}
	
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
