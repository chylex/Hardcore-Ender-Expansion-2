package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.BlockAbstractTable.Companion.TIER
import chylex.hee.system.util.Pos
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.getLongArray
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setLongArray
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos

abstract class TileEntityBaseTable : TileEntityBase(), ITickable{
	private companion object{
		private const val MAX_PEDESTAL_DISTANCE = 6
		private const val MAX_PEDESTAL_DISTANCE_SQ = MAX_PEDESTAL_DISTANCE * MAX_PEDESTAL_DISTANCE
	}
	
	abstract val tableIndicatorColor: Int
	
	private val linkedPedestals = HashSet<BlockPos>(/*initialCapacity */ 4, /*loadFactor */ 1F)
	
	private val linkedPedestalSeq
		get() = linkedPedestals.asSequence().mapNotNull { it.getTile<TileEntityTablePedestal>(world) }
	
	// Behavior (General)
	
	abstract fun tickTable()
	
	override fun firstTick(){
	}
	
	final override fun update(){
	}
	
	fun onTableDestroyed(dropTableLink: Boolean){
		for(pos in linkedPedestals){
			pos.getTile<TileEntityTablePedestal>(world)?.onTableDestroyed(dropTableLink)
		}
		
		linkedPedestals.clear() // must reset state because the method is called twice if the player is in creative mode
	}
	
	// Behavior (Linking)
	
	fun tryLinkPedestal(pedestal: TileEntityTablePedestal): Boolean{
		if (pedestal.hasLinkedTable){
			return false
		}
		
		val pedestalPos = pedestal.pos
		
		if (linkedPedestals.contains(pedestalPos) || pedestalPos.y != pos.y || pedestalPos.distanceSqTo(pos) > MAX_PEDESTAL_DISTANCE_SQ){
			return false
		}
		
		linkedPedestals.add(pedestalPos)
		pedestal.setLinkedTable(this)
		
		markDirty()
		return true
	}
	
	fun notifyPedestalUnlinked(pedestal: TileEntityTablePedestal){
		if (linkedPedestals.remove(pedestal.pos)){
			markDirty()
		}
	}
	
	// Serialization
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		setLongArray("PedestalPos", linkedPedestals.map(BlockPos::toLong).toLongArray())
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		linkedPedestals.clear()
		getLongArray("PedestalPos").forEach { linkedPedestals.add(Pos(it)) }
	}
}
