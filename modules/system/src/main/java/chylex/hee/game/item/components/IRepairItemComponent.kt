package chylex.hee.game.item.components

import net.minecraft.item.ItemStack

fun interface IRepairItemComponent {
	fun isRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean
}
