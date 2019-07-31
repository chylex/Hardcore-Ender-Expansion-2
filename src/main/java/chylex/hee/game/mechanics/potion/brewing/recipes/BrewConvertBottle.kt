package chylex.hee.game.mechanics.potion.brewing.recipes
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import chylex.hee.system.util.nbtOrNull
import chylex.hee.system.util.size
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

sealed class BrewConvertBottle(private val reagent: Item, private val newContainer: Item) : IBrewingRecipe{
	override fun isInput(input: ItemStack): Boolean{
		return input.item === Items.POTIONITEM && PotionBrewing.unpack(input) != null
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return ingredient.item === reagent
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		return ItemStack(newContainer, input.size, input.metadata).also {
			it.tagCompound = input.nbtOrNull?.copy()
		}
	}
	
	object IntoSplash : BrewConvertBottle(Items.GUNPOWDER, Items.SPLASH_POTION)
	object IntoLingering : BrewConvertBottle(Items.DRAGON_BREATH, Items.LINGERING_POTION)
}
