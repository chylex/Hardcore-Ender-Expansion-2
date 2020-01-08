package chylex.hee.game.recipe
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.init.ModItems
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.size
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object RecipePortalTokenDuplication : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int): Boolean{
		return (width * height) >= 2
	}
	
	override fun matches(inv: CraftingInventory, world: World): Boolean{
		return determineDuplicationInfo(inv) != null
	}
	
	override fun getCraftingResult(inv: CraftingInventory): ItemStack{
		val (blankTokenCount, originalToken) = determineDuplicationInfo(inv) ?: return ItemStack.EMPTY
		
		return originalToken.copy().also {
			it.size = 1 + blankTokenCount
		}
	}
	
	// Duplication info
	
	private data class DuplicationInfo(val blankTokenCount: Int, val originalToken: ItemStack)
	
	private fun determineDuplicationInfo(inv: CraftingInventory): DuplicationInfo?{
		var originalToken: ItemStack? = null
		var blankTokenCount = 0
		
		for((_, stack) in inv.nonEmptySlots){
			val item = stack.item
			
			if (item === ModItems.PORTAL_TOKEN){
				if (originalToken != null || ModItems.PORTAL_TOKEN.getTokenType(stack) != NORMAL || !ModItems.PORTAL_TOKEN.hasTerritoryInstance(stack)){
					return null
				}
				
				originalToken = stack
				continue
			}
			
			if (item == ModItems.BLANK_TOKEN){
				++blankTokenCount
				continue
			}
			
			return null
		}
		
		if (originalToken == null || blankTokenCount == 0){
			return null
		}
		
		return DuplicationInfo(blankTokenCount, originalToken)
	}
}
