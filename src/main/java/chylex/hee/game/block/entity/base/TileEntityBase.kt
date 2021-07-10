package chylex.hee.game.block.entity.base

import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.world.getState
import chylex.hee.system.delegate.NotifyOnChange
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.heeTag
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SUpdateTileEntityPacket
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType

abstract class TileEntityBase(type: TileEntityType<out TileEntityBase>) : TileEntity(type) {
	protected companion object {
		const val FLAG_MARK_DIRTY = 128
	}
	
	// Synchronization
	
	protected fun <T> Notifying(initialValue: T, notifyFlags: Int) = NotifyOnChange(initialValue) {
		-> notifyUpdate(notifyFlags)
	}
	
	val wrld
		get() = super.world!!
	
	protected var isLoaded = false
		private set
	
	final override fun onLoad() {
		super.onLoad()
		isLoaded = true
	}
	
	protected fun notifyUpdate(flags: Int) {
		if (super.world == null || wrld.isRemote || !isLoaded) {
			return
		}
		
		val pos = pos
		val state = pos.getState(wrld)
		
		wrld.notifyBlockUpdate(pos, state, state, flags and FLAG_MARK_DIRTY.inv())
		
		if (flags and FLAG_MARK_DIRTY != 0) {
			markDirty()
		}
	}
	
	// NBT
	
	protected enum class Context {
		STORAGE, NETWORK
	}
	
	protected abstract fun writeNBT(nbt: TagCompound, context: Context)
	protected abstract fun readNBT(nbt: TagCompound, context: Context)
	
	// NBT: Storage
	
	final override fun write(nbt: TagCompound): TagCompound {
		return super.write(nbt).also { writeNBT(it.heeTag, STORAGE) }
	}
	
	final override fun read(state: BlockState, nbt: CompoundNBT) {
		super.read(state, nbt)
		readNBT(nbt.heeTag, STORAGE)
	}
	
	// NBT: Network load (Note: do not use super.getUpdateTag/handleUpdateTag to prevent a duplicate client-side call)
	
	final override fun getUpdateTag(): TagCompound {
		return super.write(TagCompound()).also { writeNBT(it.heeTag, NETWORK) }
	}
	
	final override fun handleUpdateTag(state: BlockState, nbt: CompoundNBT) {
		super.read(state, nbt)
		readNBT(nbt.heeTag, NETWORK)
	}
	
	// NBT: Network update
	
	override fun getUpdatePacket(): SUpdateTileEntityPacket {
		return SUpdateTileEntityPacket(pos, 0, TagCompound().also { writeNBT(it, NETWORK) })
	}
	
	override fun onDataPacket(net: NetworkManager, packet: SUpdateTileEntityPacket) {
		readNBT(packet.nbtCompound, NETWORK)
	}
}
