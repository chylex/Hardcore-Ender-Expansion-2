package chylex.hee.game.mechanics.potion.brewing
import net.minecraft.item.ItemStack

interface IBrewingRecipe : net.minecraftforge.common.brewing.IBrewingRecipe{
	@JvmDefault
	override fun getOutput(input: ItemStack, ingredient: ItemStack): ItemStack{
		return if (isIngredient(ingredient) && isInput(input))
			brew(input, ingredient)
		else
			ItemStack.EMPTY
	}
	
	fun brew(input: ItemStack, ingredient: ItemStack): ItemStack
}
