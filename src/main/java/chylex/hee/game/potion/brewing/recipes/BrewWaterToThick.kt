package chylex.hee.game.potion.brewing.recipes

import chylex.hee.game.potion.brewing.IBrewingRecipe
import chylex.hee.game.potion.brewing.PotionItems
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.Potions

object BrewWaterToThick : IBrewingRecipe {
	override fun isInput(input: ItemStack): Boolean {
		return PotionItems.checkBottle(input, Potions.WATER)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean {
		return ingredient.item === Items.GLOWSTONE_DUST
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack {
		return PotionItems.getBottle(input.item, Potions.THICK)
	}
}
