package chylex.hee.game.mechanics.potion.brewing.recipes
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewBasicEffects.FromAwkward
import net.minecraft.init.PotionTypes.MUNDANE
import net.minecraft.init.PotionTypes.WATER
import net.minecraft.item.ItemStack

object BrewWaterToMundane : IBrewingRecipe{
	override fun isInput(input: ItemStack): Boolean{
		return PotionItems.checkBottle(input, WATER)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return FromAwkward.isIngredient(ingredient)
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		return PotionItems.getBottle(input.item, MUNDANE)
	}
}
