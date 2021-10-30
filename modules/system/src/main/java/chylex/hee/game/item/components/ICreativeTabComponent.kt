package chylex.hee.game.item.components

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

fun interface ICreativeTabComponent {
	fun addItems(tab: NonNullList<ItemStack>, item: Item)
}
