package chylex.hee.game.block.entity
import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableInputTransformer.Companion.CONSUME_STACK
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcess.Companion.NO_DUST
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Cancel
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Work
import chylex.hee.game.mechanics.table.process.ProcessOnePedestal
import chylex.hee.game.mechanics.table.process.serializer.BasicProcessSerializer
import chylex.hee.system.util.color.RGB
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TileEntityAccumulationTable : TileEntityBaseTable(){
	override val tableIndicatorColor = RGB(220, 89, 55).toInt()
	
	override val processTickRate = 3
	override val processSerializer = BasicProcessSerializer(::Process)
	
	override fun createNewProcesses(unassignedPedestals: List<TileEntityTablePedestal>): List<ITableProcess>{
		val newProcesses = ArrayList<Process>(1)
		
		for(pedestal in unassignedPedestals){
			if (pedestal.itemInputCopy.item is ItemAbstractEnergyUser){
				newProcesses.add(Process(world, pedestal.pos))
			}
		}
		
		return newProcesses
	}
	
	private class Process : ProcessOnePedestal{
		constructor(world: World, pos: BlockPos) : super(world, pos)
		constructor(world: World, nbt: NBTTagCompound) : super(world, nbt)
		
		override val energyPerTick =
			Units(1)
		
		override val dustPerTick =
			NO_DUST
		
		override val whenFinished =
			CONSUME_STACK
		
		override fun isInputStillValid(oldInput: ItemStack, newInput: ItemStack): Boolean{
			return oldInput.item === newInput.item
		}
		
		override fun onWorkTick(context: ITableContext, input: ItemStack): State{
			val item = input.item
			
			if (item !is ItemAbstractEnergyUser){
				return Cancel
			}
			
			if (item.hasMaximumEnergy(input)){
				return Output(input)
			}
			
			if (!context.requestUseResources()){
				return Work.Blocked
			}
			
			item.chargeEnergyUnit(input)
			return Work.Success
		}
	}
}
