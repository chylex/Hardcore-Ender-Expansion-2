package chylex.hee.game.mechanics.potion.brewing.recipes
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import net.minecraft.init.Items
import net.minecraft.init.PotionTypes.THICK
import net.minecraft.init.PotionTypes.WATER
import net.minecraft.item.ItemStack

object BrewWaterToThick : IBrewingRecipe{
	override fun isInput(input: ItemStack): Boolean{
		return PotionItems.checkBottle(input, WATER)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return ingredient.item === Items.GLOWSTONE_DUST
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		return PotionItems.getBottle(input.item, THICK)
	}
}
