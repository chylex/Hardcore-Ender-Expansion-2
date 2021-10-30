package chylex.hee.game.container.slot

import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.mechanics.trinket.ITrinketHandlerProvider
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class SlotTrinketItemNoContainers(trinketHandler: IItemHandler, slotIndex: Int, x: Int, y: Int) : SlotTrinketItem(trinketHandler, slotIndex, x, y) {
	override fun isItemValid(stack: ItemStack): Boolean {
		return super.isItemValid(stack) && stack.item.getHeeInterface<ITrinketHandlerProvider>() == null
	}
}
