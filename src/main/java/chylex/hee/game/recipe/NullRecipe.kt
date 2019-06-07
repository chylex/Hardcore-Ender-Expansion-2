package chylex.hee.game.recipe
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class NullRecipe : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int) = false
	override fun matches(inv: InventoryCrafting, world: World) = false
	override fun getCraftingResult(inv: InventoryCrafting): ItemStack = ItemStack.EMPTY
}
