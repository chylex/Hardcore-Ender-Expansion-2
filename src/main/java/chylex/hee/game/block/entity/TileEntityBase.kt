package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

abstract class TileEntityBase : TileEntity(){
	protected companion object{
		const val FLAG_MARK_DIRTY = 128
	}
	
	protected open fun firstTick(){}
	
	// Synchronization
	
	protected inner class Notifying<T>(initialValue: T, private val notifyFlags: Int) : ObservableProperty<T>(initialValue){
		override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T){
			if (isLoaded && hasWorld() && !world.isRemote){
				notifyUpdate(notifyFlags and FLAG_MARK_DIRTY.inv())
				
				if (notifyFlags and FLAG_MARK_DIRTY != 0){
					markDirty()
				}
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
	
	protected enum class Context{
		STORAGE, NETWORK
	}
	
	protected abstract fun writeNBT(nbt: NBTTagCompound, context: Context)
	protected abstract fun readNBT(nbt: NBTTagCompound, context: Context)
	
	// NBT: Storage
	
	final override fun writeToNBT(nbt: NBTTagCompound): NBTTagCompound{
		return super.writeToNBT(nbt).also { writeNBT(it.heeTag, STORAGE) }
	}
	
	final override fun readFromNBT(nbt: NBTTagCompound){
		super.readFromNBT(nbt)
		readNBT(nbt.heeTag, STORAGE)
	}
	
	// NBT: Network load (Note: do not use super.getUpdateTag/handleUpdateTag to prevent a duplicate client-side call)
	
	final override fun getUpdateTag(): NBTTagCompound{
		return super.writeToNBT(NBTTagCompound()).also { writeNBT(it.heeTag, NETWORK) }
	}
	
	final override fun handleUpdateTag(nbt: NBTTagCompound){
		super.readFromNBT(nbt)
		readNBT(nbt.heeTag, NETWORK)
	}
	
	// NBT: Network update
	
	override fun getUpdatePacket(): SPacketUpdateTileEntity{
		return SPacketUpdateTileEntity(pos, 0, NBTTagCompound().also { writeNBT(it, NETWORK) })
	}
	
	override fun onDataPacket(net: NetworkManager, packet: SPacketUpdateTileEntity){
		readNBT(packet.nbtCompound, NETWORK)
	}
}
