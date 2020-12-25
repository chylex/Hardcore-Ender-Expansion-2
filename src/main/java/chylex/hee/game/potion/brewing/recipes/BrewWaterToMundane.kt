package chylex.hee.game.potion.brewing.recipes

import chylex.hee.game.potion.brewing.IBrewingRecipe
import chylex.hee.game.potion.brewing.PotionItems
import chylex.hee.game.potion.brewing.recipes.BrewBasicEffects.FromAwkward
import chylex.hee.system.migration.PotionTypes
import net.minecraft.item.ItemStack

object BrewWaterToMundane : IBrewingRecipe {
	override fun isInput(input: ItemStack): Boolean {
		return PotionItems.checkBottle(input, PotionTypes.WATER)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean {
		return FromAwkward.isIngredient(ingredient)
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack {
		return PotionItems.getBottle(input.item, PotionTypes.MUNDANE)
	}
}
