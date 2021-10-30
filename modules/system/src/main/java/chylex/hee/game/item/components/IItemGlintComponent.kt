package chylex.hee.game.item.components

import net.minecraft.item.ItemStack

fun interface IItemGlintComponent {
	fun hasGlint(stack: ItemStack): Boolean
	
	companion object {
		fun either(left: IItemGlintComponent, right: IItemGlintComponent) = IItemGlintComponent {
			left.hasGlint(it) || right.hasGlint(it)
		}
	}
}
