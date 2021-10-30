package chylex.hee.game.item.container

import chylex.hee.game.item.interfaces.IItemInterface
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack

fun interface IItemWithContainer : IItemInterface {
	fun createContainerProvider(stack: ItemStack, slot: Int): INamedContainerProvider
}
