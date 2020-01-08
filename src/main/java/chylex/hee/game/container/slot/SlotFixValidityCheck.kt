package chylex.hee.game.container.slot
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

class SlotFixValidityCheck(wrapped: Slot) : SlotWrapper(wrapped){
	override fun isItemValid(stack: ItemStack) = inventory.isItemValidForSlot(slotIndex, stack)
}
