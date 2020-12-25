package chylex.hee.game.mechanics.table.process

import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.BLOCKED
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcess.Companion.NO_DUST
import chylex.hee.game.world.getTile
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getPos
import chylex.hee.system.serialization.putPos
import net.minecraft.util.math.BlockPos

class ProcessSupportingItemBlocker(private val table: TileEntityBaseTable, pos: BlockPos) : ITableProcess {
	constructor(table: TileEntityBaseTable, nbt: TagCompound) : this(table, nbt.getPos("PedestalPos"))
	
	override val pedestals = arrayOf(pos)
	
	override val energyPerTick = Units(0)
	override val dustPerTick = NO_DUST
	
	private val pedestalTile
		get() = pedestals[0].getTile<TileEntityTablePedestal>(table.wrld)
	
	// Handling
	
	override fun initialize() {
		pedestalTile!!.updateProcessStatus(BLOCKED)
	}
	
	override fun revalidate(): Boolean {
		return pedestalTile?.hasInputItem == true && table.totalFreePedestals == 0
	}
	
	override fun tick(context: ITableContext) {}
	
	override fun dispose() {
		pedestalTile?.updateProcessStatus(null)
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putPos("PedestalPos", pedestals[0])
	}
	
	override fun deserializeNBT(nbt: TagCompound) {}
}
