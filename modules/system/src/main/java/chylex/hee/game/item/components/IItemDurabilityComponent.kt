package chylex.hee.game.item.components

import net.minecraft.item.ItemStack
import net.minecraft.util.math.MathHelper

interface IItemDurabilityComponent {
	fun showBar(stack: ItemStack): Boolean {
		return stack.isDamaged
	}
	
	fun getDisplayDurability(stack: ItemStack): Double {
		return stack.damage.toDouble() / stack.maxDamage.toDouble()
	}
	
	fun getDisplayDurabilityRGB(stack: ItemStack): Int {
		return MathHelper.hsvToRGB((1F - getDisplayDurability(stack)).toFloat().coerceAtLeast(0F) / 3F, 1F, 1F)
	}
}
