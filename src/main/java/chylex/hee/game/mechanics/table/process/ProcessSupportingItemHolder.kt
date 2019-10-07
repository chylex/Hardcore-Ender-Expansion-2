package chylex.hee.game.mechanics.table.process
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.SUPPORTING_ITEM
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcess.Companion.NO_DUST
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getPos
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setPos
import chylex.hee.system.util.size
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ProcessSupportingItemHolder(private val world: World, pos: BlockPos) : ITableProcess{
	constructor(world: World, nbt: TagCompound) : this(world, nbt.getPos("PedestalPos"))
	
	override val pedestals = arrayOf(pos)
	
	override val energyPerTick = Units(0)
	override val dustPerTick = NO_DUST
	
	private val pedestalTile
		get() = pedestals[0].getTile<TileEntityTablePedestal>(world)
	
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
	
	fun useItem(item: Item, amount: Int): Boolean{
		val tile = pedestalTile ?: return false
		val input = tile.itemInputCopy.takeIf { it.item === item && it.size >= amount } ?: return false
		
		input.shrink(amount)
		tile.replaceInput(input, silent = false)
		return true
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		setPos("PedestalPos", pedestals[0])
	}
	
	override fun deserializeNBT(nbt: TagCompound){}
}
