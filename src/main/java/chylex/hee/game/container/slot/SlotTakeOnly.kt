package chylex.hee.game.container.slot

import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

class SlotTakeOnly(wrapped: Slot) : SlotWrapper(wrapped) {
	override fun isItemValid(stack: ItemStack): Boolean {
		return false
	}
	
	override fun getSlotStackLimit(): Int {
		return 0
	}
}
