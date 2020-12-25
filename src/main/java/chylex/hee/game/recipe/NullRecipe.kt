package chylex.hee.game.recipe

import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object NullRecipe : RecipeBaseDynamic() {
	override fun canFit(width: Int, height: Int) = false
	override fun matches(inv: CraftingInventory, world: World) = false
	override fun getCraftingResult(inv: CraftingInventory): ItemStack = ItemStack.EMPTY
}
