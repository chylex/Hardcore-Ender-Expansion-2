package chylex.hee.game.potion.brewing.recipes

import chylex.hee.game.potion.brewing.IBrewingRecipe
import chylex.hee.game.potion.brewing.PotionItems
import chylex.hee.game.potion.brewing.recipes.BrewBasicEffects.FromAwkward
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potions

object BrewWaterToMundane : IBrewingRecipe {
	override fun isInput(input: ItemStack): Boolean {
		return PotionItems.checkBottle(input, Potions.WATER)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean {
		return FromAwkward.isIngredient(ingredient)
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack {
		return PotionItems.getBottle(input.item, Potions.MUNDANE)
	}
}
