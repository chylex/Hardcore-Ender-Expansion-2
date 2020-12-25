package chylex.hee.game.mechanics.trinket

import net.minecraft.item.ItemStack

interface ITrinketHandler {
	fun isInTrinketSlot(stack: ItemStack): Boolean
	fun isItemActive(item: ITrinketItem): Boolean
	fun transformIfActive(item: ITrinketItem, transformer: (ItemStack) -> Unit)
}
