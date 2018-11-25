package chylex.hee.game.mechanics.table.process
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Output
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class ProcessOnePedestal(world: World, pos: BlockPos) : ProcessManyPedestals(world, arrayOf(pos)){
	protected abstract fun isInputStillValid(oldInput: ItemStack, newInput: ItemStack): Boolean
	protected abstract fun onWorkTick(context: ITableContext, input: ItemStack): State
	
	override fun isInputStillValid(oldInput: Array<ItemStack>, newInput: Array<ItemStack>): Boolean{
		return isInputStillValid(oldInput[0], newInput[0])
	}
	
	override fun onWorkTick(context: ITableContext, inputs: Array<ItemStack>): State{
		return onWorkTick(context, inputs[0])
	}
	
	protected fun Output(stacks: Array<ItemStack>) = Output(stacks, pedestals[0])
	protected fun Output(stack: ItemStack) = Output(stack, pedestals[0])
	
	companion object{
		fun <T : ProcessOnePedestal> construct(constructor: (World, BlockPos) -> T, world: World, nbt: NBTTagCompound): T{
			return ProcessManyPedestals.construct({ alsoWorld, positions -> constructor(alsoWorld, positions[0]) }, world, nbt)
		}
	}
}
