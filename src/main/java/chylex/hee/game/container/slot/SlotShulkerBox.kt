package chylex.hee.game.container.slot
import chylex.hee.system.migration.BlockShulkerBox
import net.minecraft.block.Block
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

class SlotShulkerBox(wrapped: Slot) : SlotWrapper(wrapped){
	override fun isItemValid(stack: ItemStack) = Block.getBlockFromItem(stack.item) !is BlockShulkerBox
}
