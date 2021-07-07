package chylex.hee.game.inventory.util

import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.wrapper.InvWrapper

/**
 * Wrapper for [IItemHandlerModifiable] that reverses the slot order.
 */
class InvReverseWrapper(private val wrapped: IItemHandlerModifiable) : IItemHandlerModifiable {
	constructor(inventory: IInventory) : this(InvWrapper(inventory))
	
	private fun reverse(slot: Int) = slots - slot - 1
	
	override fun getSlots() = wrapped.slots
	override fun getStackInSlot(slot: Int) = wrapped.getStackInSlot(reverse(slot))
	override fun setStackInSlot(slot: Int, stack: ItemStack) = wrapped.setStackInSlot(reverse(slot), stack)
	override fun isItemValid(slot: Int, stack: ItemStack) = wrapped.isItemValid(reverse(slot), stack)
	override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean) = wrapped.insertItem(reverse(slot), stack, simulate)
	override fun extractItem(slot: Int, amount: Int, simulate: Boolean) = wrapped.extractItem(reverse(slot), amount, simulate)
	override fun getSlotLimit(slot: Int) = wrapped.getSlotLimit(reverse(slot))
	
	override fun equals(other: Any?) = other is InvReverseWrapper && other.wrapped == wrapped
	override fun hashCode() = wrapped.hashCode()
	override fun toString() = wrapped.toString()
}
