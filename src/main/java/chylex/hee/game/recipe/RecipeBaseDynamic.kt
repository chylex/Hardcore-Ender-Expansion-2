package chylex.hee.game.recipe
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.registries.IForgeRegistryEntry

abstract class RecipeBaseDynamic : IForgeRegistryEntry.Impl<IRecipe>(), IRecipe{
	final override fun isDynamic(): Boolean = true
	final override fun getRecipeOutput(): ItemStack = ItemStack.EMPTY
}
