package chylex.hee.game.block.entity

import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.mechanics.energy.IEnergyItem
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableInputTransformer.Companion.CONSUME_STACK
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcess.Companion.NO_DUST
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Cancel
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Work
import chylex.hee.game.mechanics.table.process.ProcessOnePedestal
import chylex.hee.game.mechanics.table.process.serializer.BasicProcessSerializer
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModTileEntities
import chylex.hee.util.color.RGB
import chylex.hee.util.nbt.TagCompound
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos

class TileEntityAccumulationTable(type: TileEntityType<TileEntityAccumulationTable>) : TileEntityBaseTable(type) {
	@Suppress("unused")
	constructor() : this(ModTileEntities.ACCUMULATION_TABLE)
	
	object Type : IHeeTileEntityType<TileEntityAccumulationTable> {
		override val blocks
			get() = arrayOf(
				ModBlocks.ACCUMULATION_TABLE_TIER_1,
				ModBlocks.ACCUMULATION_TABLE_TIER_2,
				ModBlocks.ACCUMULATION_TABLE_TIER_3,
			)
	}
	
	override val tableIndicatorColor = RGB(220, 89, 55)
	
	override val processTickRate = 3
	override val processSerializer = BasicProcessSerializer(::Process)
	
	override fun createNewProcesses(unassignedPedestals: List<TileEntityTablePedestal>): List<ITableProcess> {
		val newProcesses = ArrayList<Process>(1)
		
		for (pedestal in unassignedPedestals) {
			if (pedestal.itemInputCopy.item.getHeeInterface<IEnergyItem>() != null) {
				newProcesses.add(Process(this, pedestal.pos))
			}
		}
		
		return newProcesses
	}
	
	private class Process : ProcessOnePedestal {
		constructor(table: TileEntityBaseTable, pos: BlockPos) : super(table, pos)
		constructor(table: TileEntityBaseTable, nbt: TagCompound) : super(table, nbt)
		
		override val energyPerTick =
			Units(1)
		
		override val dustPerTick =
			NO_DUST
		
		override val whenFinished =
			CONSUME_STACK
		
		override fun isInputStillValid(oldInput: ItemStack, newInput: ItemStack): Boolean {
			return oldInput.item === newInput.item
		}
		
		override fun onWorkTick(context: ITableContext, input: ItemStack): State {
			val energy = input.item.getHeeInterface<IEnergyItem>()
			if (energy == null) {
				return Cancel
			}
			
			if (energy.hasMaximumEnergy(input)) {
				return Output(input)
			}
			
			if (!context.requestUseResources()) {
				return Work.Blocked
			}
			
			energy.chargeUnit(input)
			return Work.Success
		}
	}
}
