package chylex.hee.game.item.components

import net.minecraft.item.ItemStack

object EnchantmentGlintComponent : IItemGlintComponent {
	override fun hasGlint(stack: ItemStack): Boolean {
		return stack.isEnchanted
	}
}
