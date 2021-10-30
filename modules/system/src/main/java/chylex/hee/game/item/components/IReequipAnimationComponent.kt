package chylex.hee.game.item.components

import net.minecraft.item.ItemStack

interface IReequipAnimationComponent {
	fun shouldAnimate(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
		return oldStack != newStack
	}
	
	object AnimateIfSlotChanged : IReequipAnimationComponent {
		@Suppress("KotlinConstantConditions")
		override fun shouldAnimate(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
			return slotChanged && super.shouldAnimate(oldStack, newStack, slotChanged)
		}
	}
}
