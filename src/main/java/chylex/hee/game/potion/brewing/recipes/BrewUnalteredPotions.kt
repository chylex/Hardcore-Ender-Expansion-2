package chylex.hee.game.potion.brewing.recipes
import chylex.hee.game.potion.brewing.IBrewingRecipe
import chylex.hee.game.potion.brewing.PotionBrewing
import net.minecraft.item.ItemStack
import net.minecraftforge.common.brewing.VanillaBrewingRecipe

object BrewUnalteredPotions : IBrewingRecipe{
	private val vanilla = VanillaBrewingRecipe()
	
	override fun isInput(input: ItemStack): Boolean{
		return vanilla.isInput(input)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return vanilla.isIngredient(ingredient)
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		return vanilla.getOutput(input, ingredient).takeUnless(PotionBrewing::isAltered) ?: ItemStack.EMPTY
	}
}
