package chylex.hee.game.potion.brewing.recipes

import chylex.hee.game.potion.brewing.IBrewingRecipe
import chylex.hee.game.potion.brewing.PotionBrewing
import chylex.hee.game.potion.brewing.PotionItems
import net.minecraft.item.ItemStack

object ReinsertPotionItems : IBrewingRecipe {
	override fun isInput(input: ItemStack): Boolean {
		return PotionItems.isPotion(input) && PotionBrewing.unpack(input) != null // allows potions to be placed back into Brewing Stands
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean {
		return false
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack {
		return ItemStack.EMPTY
	}
}
