package chylex.hee.game.potion.brewing.modifiers

import chylex.hee.game.potion.brewing.IBrewingModifier
import chylex.hee.game.potion.brewing.PotionBrewing
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object BrewIncreaseDuration : IBrewingModifier {
	override val ingredient = Items.REDSTONE!!
	
	override fun check(input: ItemStack): Boolean {
		return PotionBrewing.unpack(input)?.canIncreaseDuration == true
	}
	
	override fun apply(input: ItemStack): ItemStack {
		return PotionBrewing.unpack(input)!!.withIncreasedDuration
	}
}
