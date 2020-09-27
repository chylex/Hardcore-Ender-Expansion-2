package chylex.hee.game.mechanics.table.process
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.inventory.size
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.SUPPORTING_ITEM
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcess.Companion.NO_DUST
import chylex.hee.game.world.getTile
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getPos
import chylex.hee.system.serialization.putPos
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class ProcessSupportingItemHolder(private val table: TileEntityBaseTable, pos: BlockPos) : ITableProcess{
	constructor(table: TileEntityBaseTable, nbt: TagCompound) : this(table, nbt.getPos(PEDESTAL_POS_TAG))
	
	private companion object{
		private const val PEDESTAL_POS_TAG = "PedestalPos"
	}
	
	override val pedestals = arrayOf(pos)
	
	override val energyPerTick = Units(0)
	override val dustPerTick = NO_DUST
	
	val pedestalPos
		get() = pedestals[0]
	
	private val pedestalTile
		get() = pedestals[0].getTile<TileEntityTablePedestal>(table.wrld)
	
	// Handling
	
	override fun initialize(){
		pedestalTile!!.updateProcessStatus(SUPPORTING_ITEM)
	}
	
	override fun revalidate(): Boolean{
		return pedestalTile?.hasInputItem == true
	}
	
	override fun tick(context: ITableContext){}
	
	override fun dispose(){
		pedestalTile?.updateProcessStatus(null)
	}
	
	// Methods
	
	fun useItem(getRequiredAmount: (ItemStack) -> Int): ItemStack?{
		val tile = pedestalTile ?: return null
		val input = tile.itemInputCopy
		
		val testCopy = input.copy()
		val useAmount = getRequiredAmount(testCopy)
		
		if (useAmount == 0 || input.size < useAmount){
			return null
		}
		
		input.shrink(useAmount)
		tile.replaceInput(input, silent = false)
		
		return testCopy.also { it.size = useAmount }
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putPos(PEDESTAL_POS_TAG, pedestals[0])
	}
	
	override fun deserializeNBT(nbt: TagCompound){}
}
