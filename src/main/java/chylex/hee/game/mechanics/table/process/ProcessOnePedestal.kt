package chylex.hee.game.mechanics.table.process

import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Output
import chylex.hee.system.serialization.TagCompound
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

abstract class ProcessOnePedestal : ProcessManyPedestals {
	protected constructor(table: TileEntityBaseTable, pos: BlockPos) : super(table, arrayOf(pos))
	protected constructor(table: TileEntityBaseTable, nbt: TagCompound) : super(table, nbt)
	
	protected abstract fun isInputStillValid(oldInput: ItemStack, newInput: ItemStack): Boolean
	protected abstract fun onWorkTick(context: ITableContext, input: ItemStack): State
	
	override fun isInputStillValid(oldInput: Array<ItemStack>, newInput: Array<ItemStack>): Boolean {
		return isInputStillValid(oldInput[0], newInput[0])
	}
	
	override fun onWorkTick(context: ITableContext, inputs: Array<ItemStack>): State {
		return onWorkTick(context, inputs[0])
	}
	
	protected fun Output(stacks: Array<ItemStack>) = Output(stacks, pedestals[0])
	protected fun Output(stack: ItemStack) = Output(stack, pedestals[0])
}
