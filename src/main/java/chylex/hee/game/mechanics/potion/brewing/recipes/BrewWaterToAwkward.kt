package chylex.hee.game.mechanics.potion.brewing.recipes
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.system.migration.vanilla.Items
import net.minecraft.init.PotionTypes.AWKWARD
import net.minecraft.init.PotionTypes.WATER
import net.minecraft.item.ItemStack

object BrewWaterToAwkward : IBrewingRecipe{
	override fun isInput(input: ItemStack): Boolean{
		return PotionItems.checkBottle(input, WATER)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return ingredient.item === Items.NETHER_WART
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		return PotionItems.getBottle(input.item, AWKWARD)
	}
}
