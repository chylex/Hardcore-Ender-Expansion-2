package chylex.hee.game.recipe

import chylex.hee.game.inventory.util.getStack
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.ICraftingRecipe
import net.minecraft.item.crafting.SpecialRecipeSerializer

abstract class RecipeBaseDynamic : ICraftingRecipe {
	private val serializer = SpecialRecipeSerializer { this }
	
	final override fun getId() = serializer.registryName
	final override fun getSerializer() = serializer
	
	final override fun isDynamic() = true
	final override fun getRecipeOutput(): ItemStack = ItemStack.EMPTY
	
	protected fun getStackInRowAndColumn(inv: CraftingInventory, row: Int, column: Int): ItemStack {
		return inv.getStack(column + (row * inv.width))
	}
}
