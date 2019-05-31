package chylex.hee.game.container.slot
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class SlotReadOnly(wrapped: Slot) : SlotWrapper(wrapped){
	override fun isItemValid(stack: ItemStack) = false
	override fun getSlotStackLimit() = 0
}
