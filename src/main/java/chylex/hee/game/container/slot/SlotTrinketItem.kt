package chylex.hee.game.container.slot

import chylex.hee.game.Resource
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.init.ModAtlases
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.SlotItemHandler

open class SlotTrinketItem(trinketHandler: IItemHandler, slotIndex: Int, x: Int, y: Int) : SlotItemHandler(trinketHandler, slotIndex, x, y) {
	companion object {
		val TEX_SLOT_OVERLAY = Resource.Custom("gui/slot_trinket")
	}
	
	init {
		@Suppress("LeakingThis")
		setBackground(ModAtlases.ATLAS_GUIS, TEX_SLOT_OVERLAY)
	}
	
	override fun isItemValid(stack: ItemStack): Boolean {
		return (stack.item as? ITrinketItem)?.canPlaceIntoTrinketSlot(stack) == true
	}
	
	override fun getSlotStackLimit(): Int {
		return 1
	}
}
