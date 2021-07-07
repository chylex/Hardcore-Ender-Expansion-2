package chylex.hee.game.container.slot

import net.minecraft.block.Block
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

class SlotShulkerBox(wrapped: Slot) : SlotWrapper(wrapped) {
	override fun isItemValid(stack: ItemStack): Boolean {
		return Block.getBlockFromItem(stack.item) !is ShulkerBoxBlock
	}
}
