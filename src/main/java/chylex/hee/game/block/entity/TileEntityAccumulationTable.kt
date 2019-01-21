package chylex.hee.game.block.entity
import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.process.ITableContext
import chylex.hee.game.mechanics.table.process.ITableInputTransformer.Companion.CONSUME_STACK
import chylex.hee.game.mechanics.table.process.ITableProcess
import chylex.hee.game.mechanics.table.process.ITableProcess.Companion.NO_DUST
import chylex.hee.game.mechanics.table.process.ITableProcessSerializer
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Cancel
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Work
import chylex.hee.game.mechanics.table.process.ProcessOnePedestal
import chylex.hee.game.render.util.RGB
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TileEntityAccumulationTable : TileEntityBaseTable(){
	override val tableIndicatorColor = RGB(220, 89, 55).toInt()
	
	override val processSerializer: ITableProcessSerializer = Serializer
	override val processTickRate = 3
	
	override fun createNewProcesses(unassignedPedestals: List<TileEntityTablePedestal>): List<ITableProcess>{
		val newProcesses = ArrayList<Process>(1)
		
		for(pedestal in unassignedPedestals){
			if (pedestal.itemInputCopy.item is ItemAbstractEnergyUser){
				newProcesses.add(Process(world, pedestal.pos))
			}
		}
		
		return newProcesses
	}
	
	private object Serializer : ITableProcessSerializer{
		override fun writeToNBT(process: ITableProcess): NBTTagCompound{
			return process.serializeNBT()
		}
		
		override fun readFromNBT(world: World, nbt: NBTTagCompound): ITableProcess{
			return Process(world, nbt).also { it.deserializeNBT(nbt) }
		}
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
			
			if (context.requestUseResources()){
				item.chargeEnergyUnit(input)
			}
			
			return Work
		}
	}
}
