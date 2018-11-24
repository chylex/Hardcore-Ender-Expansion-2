package chylex.hee.game.mechanics.table
import chylex.hee.game.block.entity.TileEntityBaseTable
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.system.util.Pos
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.getLongArray
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setLongArray
import chylex.hee.system.util.square
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

class TableLinkedPedestalHandler(private val table: TileEntityBaseTable<*>, maxDistance: Int) : INBTSerializable<NBTTagCompound>{
	private val maxDistanceSq = square(maxDistance)
	private val pedestalPositions = HashSet<BlockPos>(/*initialCapacity */ 4, /*loadFactor */ 1F)
	
	val pedestalTiles
		get() = pedestalPositions.asSequence().mapNotNull { it.getTile<TileEntityTablePedestal>(table.world) }
	
	fun tryLinkPedestal(pedestal: TileEntityTablePedestal): Boolean{
		if (pedestal.hasLinkedTable){
			return false
		}
		
		val tablePos = table.pos
		val pedestalPos = pedestal.pos
		
		if (pedestalPositions.size >= table.maxInputPedestals || pedestalPositions.contains(pedestalPos) || pedestalPos.y != tablePos.y || pedestalPos.distanceSqTo(tablePos) > maxDistanceSq){
			return false
		}
		
		pedestalPositions.add(pedestalPos)
		pedestal.setLinkedTable(table)
		
		table.markDirty()
		return true
	}
	
	fun tryUnlinkPedestal(pedestal: TileEntityTablePedestal, dropTableLink: Boolean): Boolean{
		if (pedestalPositions.remove(pedestal.pos)){
			pedestal.onTableUnlinked(table, dropTableLink)
			table.markDirty()
			return true
		}
		
		return false
	}
	
	fun onAllPedestalsUnlinked(){
		pedestalPositions.clear()
		table.markDirty()
	}
	
	// Serialization
	
	override fun serializeNBT() = NBTTagCompound().apply {
		setLongArray("Pos", pedestalPositions.map(BlockPos::toLong).toLongArray())
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		pedestalPositions.clear()
		getLongArray("Pos").forEach { pedestalPositions.add(Pos(it)) }
	}
}
