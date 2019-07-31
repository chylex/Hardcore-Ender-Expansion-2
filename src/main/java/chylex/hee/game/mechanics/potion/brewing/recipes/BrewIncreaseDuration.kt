package chylex.hee.game.mechanics.potion.brewing.recipes
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

object BrewIncreaseDuration : IBrewingRecipe{
	override fun isInput(input: ItemStack): Boolean{
		return PotionBrewing.unpack(input)?.canIncreaseDuration == true
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return ingredient.item === Items.REDSTONE
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		return PotionBrewing.unpack(input)!!.withIncreasedDuration
	}
}
