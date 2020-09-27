package chylex.hee.game.mechanics.table
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.world.Pos
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.getTile
import chylex.hee.system.math.square
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getPosOrNull
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.use
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

class TableLinkedPedestalHandler(private val table: TileEntityBaseTable, maxDistance: Double) : INBTSerializable<TagCompound>{
	private companion object{
		private const val POS_TAG = "Pos"
		private const val OUTPUT_TAG = "Output"
	}
	
	private val maxDistanceSq = square(maxDistance)
	private val inputPedestals = HashSet<BlockPos>(/*initialCapacity */ 4, /*loadFactor */ 1F)
	private var dedicatedOutputPedestal: BlockPos? = null
	
	val inputPedestalTiles
		get() = inputPedestals.mapNotNull { it.getTile<TileEntityTablePedestal>(table.wrld) }
	
	val dedicatedOutputPedestalTile
		get() = dedicatedOutputPedestal?.getTile<TileEntityTablePedestal>(table.wrld)
	
	// Behavior
	
	fun tryLinkPedestal(pedestal: TileEntityTablePedestal): Boolean{
		if (pedestal.hasLinkedTable){
			return false
		}
		
		val tablePos = table.pos
		val pedestalPos = pedestal.pos
		
		if (inputPedestals.contains(pedestalPos) || pedestalPos.y != tablePos.y || pedestalPos.distanceSqTo(tablePos) > maxDistanceSq){
			return false
		}
		
		if (inputPedestals.size >= table.maxInputPedestals){
			tryUnlinkOutputPedestal(dropTableLink = true)
			dedicatedOutputPedestal = pedestalPos
			
			pedestal.setLinkedTable(table)
			pedestal.isDedicatedOutput = true
		}
		else{
			inputPedestals.add(pedestalPos)
			pedestal.setLinkedTable(table)
		}
		
		table.markDirty()
		return true
	}
	
	fun tryUnlinkPedestal(pedestal: TileEntityTablePedestal, dropTableLink: Boolean): Boolean{
		if (pedestal.pos == dedicatedOutputPedestal){
			tryUnlinkOutputPedestal(dropTableLink)
			return true
		}
		
		if (inputPedestals.remove(pedestal.pos)){
			pedestal.onTableUnlinked(table, dropTableLink)
			table.markDirty()
			return true
		}
		
		return false
	}
	
	fun tryMarkInputPedestalAsOutput(pedestal: TileEntityTablePedestal): Boolean{
		val pedestalPos = pedestal.pos
		
		if (!inputPedestals.remove(pedestalPos)){
			return false
		}
		
		val currentOutputPedestal = dedicatedOutputPedestalTile
		
		if (currentOutputPedestal != null){
			currentOutputPedestal.isDedicatedOutput = false
			inputPedestals.add(currentOutputPedestal.pos)
		}
		
		dedicatedOutputPedestal = pedestalPos
		pedestal.isDedicatedOutput = true
		
		table.markDirty()
		return true
	}
	
	private fun tryUnlinkOutputPedestal(dropTableLink: Boolean){
		val pedestal = dedicatedOutputPedestalTile ?: return
		
		pedestal.onTableUnlinked(table, dropTableLink)
		dedicatedOutputPedestal = null
		table.markDirty()
	}
	
	fun onAllPedestalsUnlinked(){
		inputPedestals.clear()
		dedicatedOutputPedestal = null
		table.markDirty()
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putLongArray(POS_TAG, inputPedestals.map(BlockPos::toLong).toLongArray())
		
		dedicatedOutputPedestal?.let {
			putPos(OUTPUT_TAG, it)
		}
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		inputPedestals.clear()
		getLongArray(POS_TAG).forEach { inputPedestals.add(Pos(it)) }
		
		dedicatedOutputPedestal = getPosOrNull(OUTPUT_TAG)
	}
}
