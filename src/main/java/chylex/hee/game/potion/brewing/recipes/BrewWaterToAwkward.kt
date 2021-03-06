package chylex.hee.game.potion.brewing.recipes

import chylex.hee.game.potion.brewing.IBrewingRecipe
import chylex.hee.game.potion.brewing.PotionItems
import chylex.hee.system.migration.PotionTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object BrewWaterToAwkward : IBrewingRecipe {
	override fun isInput(input: ItemStack): Boolean {
		return PotionItems.checkBottle(input, PotionTypes.WATER)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean {
		return ingredient.item === Items.NETHER_WART
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack {
		return PotionItems.getBottle(input.item, PotionTypes.AWKWARD)
	}
}
