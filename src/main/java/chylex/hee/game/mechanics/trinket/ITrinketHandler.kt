package chylex.hee.game.mechanics.trinket

import net.minecraft.item.ItemStack

interface ITrinketHandler {
	fun isInTrinketSlot(stack: ItemStack): Boolean
	fun isTrinketActive(trinket: ITrinketItem): Boolean
	fun transformIfActive(trinket: ITrinketItem, transformer: (ItemStack) -> Unit)
}
