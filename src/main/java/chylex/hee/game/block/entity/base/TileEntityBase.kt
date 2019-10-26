package chylex.hee.game.block.entity.base
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity

abstract class TileEntityBase : TileEntity(){
	protected companion object{
		const val FLAG_MARK_DIRTY = 128
	}
	
	protected open fun firstTick(){}
	
	// Synchronization
	
	protected fun <T> Notifying(initialValue: T, notifyFlags: Int) = NotifyOnChange(initialValue){
		-> notifyUpdate(notifyFlags)
	}
	
	protected var isLoaded = false
		private set
	
	final override fun onLoad(){
		isLoaded = true
		firstTick()
	}
	
	protected fun notifyUpdate(flags: Int){
		if (world == null || world.isRemote || !isLoaded){
			return
		}
		
		val pos = pos
		val state = pos.getState(world)
		
		world.notifyBlockUpdate(pos, state, state, flags and FLAG_MARK_DIRTY.inv())
		
		if (flags and FLAG_MARK_DIRTY != 0){
			markDirty()
		}
	}
	
	// NBT
	
	protected enum class Context{
		STORAGE, NETWORK
	}
	
	protected abstract fun writeNBT(nbt: TagCompound, context: Context)
	protected abstract fun readNBT(nbt: TagCompound, context: Context)
	
	// NBT: Storage
	
	final override fun writeToNBT(nbt: TagCompound): TagCompound{
		return super.writeToNBT(nbt).also { writeNBT(it.heeTag, STORAGE) }
	}
	
	final override fun readFromNBT(nbt: TagCompound){
		super.readFromNBT(nbt)
		readNBT(nbt.heeTag, STORAGE)
	}
	
	// NBT: Network load (Note: do not use super.getUpdateTag/handleUpdateTag to prevent a duplicate client-side call)
	
	final override fun getUpdateTag(): TagCompound{
		return super.writeToNBT(TagCompound()).also { writeNBT(it.heeTag, NETWORK) }
	}
	
	final override fun handleUpdateTag(nbt: TagCompound){
		super.readFromNBT(nbt)
		readNBT(nbt.heeTag, NETWORK)
	}
	
	// NBT: Network update
	
	override fun getUpdatePacket(): SPacketUpdateTileEntity{
		return SPacketUpdateTileEntity(pos, 0, TagCompound().also { writeNBT(it, NETWORK) })
	}
	
	override fun onDataPacket(net: NetworkManager, packet: SPacketUpdateTileEntity){
		readNBT(packet.nbtCompound, NETWORK)
	}
}
