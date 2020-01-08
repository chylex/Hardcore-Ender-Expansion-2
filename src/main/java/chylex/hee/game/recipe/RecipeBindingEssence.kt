package chylex.hee.game.recipe
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.init.ModItems
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.size
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import kotlin.math.max

object RecipeBindingEssence : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int): Boolean{
		return (width * height) >= 2
	}
	
	override fun matches(inv: CraftingInventory, world: World): Boolean{
		return determineRepairInfo(inv) != null
	}
	
	override fun getCraftingResult(inv: CraftingInventory): ItemStack{
		val (targetItem, infusionList) = determineRepairInfo(inv) ?: return ItemStack.EMPTY
		
		return infusionList.fold(targetItem){
			acc, infusion -> infusion.tryInfuse(acc) ?: acc
		}.also {
			it.size = 1 // always applied to a clone of targetItem because infusionList cannot be empty
		}
	}
	
	// Repair info
	
	private data class RepairInfo(val targetItem: ItemStack, val infusionList: InfusionList)
	
	private fun determineRepairInfo(inv: CraftingInventory): RepairInfo?{
		var targetItem: ItemStack? = null
		var bindingEssence: ItemStack? = null
		
		for((_, stack) in inv.nonEmptySlots){
			val item = stack.item
			
			if (item === ModItems.BINDING_ESSENCE && bindingEssence == null){
				bindingEssence = stack
			}
			else if (targetItem == null){
				targetItem = stack
			}
			else{
				return null
			}
		}
		
		if (targetItem == null || bindingEssence == null){
			return null
		}
		
		val toApply = InfusionTag.getList(bindingEssence)
		
		if (toApply.isEmpty || toApply.none { it.tryInfuse(targetItem) != null }){
			return null
		}
		
		if (targetItem.item === ModItems.BINDING_ESSENCE){ // when combining two Binding Essences, one may be a subset of the other
			val toCombine = InfusionTag.getList(targetItem)
			
			if (toApply.union(toCombine).size == max(toApply.size, toCombine.size)){
				return null
			}
		}
		
		return RepairInfo(targetItem, toApply)
	}
}
