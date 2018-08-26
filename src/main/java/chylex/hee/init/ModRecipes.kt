package chylex.hee.init
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes

object ModRecipes{
	fun initialize(){ // UPDATE: Move smelting recipes to JSON
		with(FurnaceRecipes.instance()){
			addSmeltingRecipeForBlock(ModBlocks.GLOOMROCK, ItemStack(ModBlocks.GLOOMROCK_SMOOTH), 0.1F)
		}
	}
}
