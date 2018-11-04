package chylex.hee.game.recipe
import chylex.hee.init.ModItems
import chylex.hee.system.util.nonEmptySlots
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object RecipeScaleOfFreefallRepair : RecipeBaseDynamic(){
	override fun canFit(width: Int, height: Int): Boolean{
		return (width * height) >= 2
	}
	
	override fun matches(inv: InventoryCrafting, world: World): Boolean{
		return determineRepairInfo(inv) != null
	}
	
	override fun getCraftingResult(inv: InventoryCrafting): ItemStack{
		val (dragonScaleCount, damagedTrinket) = determineRepairInfo(inv) ?: return ItemStack.EMPTY
		
		return damagedTrinket.copy().also {
			it.itemDamage -= dragonScaleCount * (it.maxDamage / 2) // itemDamage < 0 is handled in Item.setDamage
		}
	}
	
	// Repair info
	
	private data class RepairInfo(val dragonScaleCount: Int, val damagedTrinket: ItemStack)
	
	private fun determineRepairInfo(inv: InventoryCrafting): RepairInfo?{
		var damagedTrinket: ItemStack? = null
		var dragonScaleCount = 0
		var dragonScaleLimit = 0
		
		for((_, stack) in inv.nonEmptySlots){
			val item = stack.item
			
			if (item === ModItems.DRAGON_SCALE){
				++dragonScaleCount
				continue
			}
			
			if (item === ModItems.SCALE_OF_FREEFALL){
				if (damagedTrinket != null || !stack.isItemDamaged){
					return null
				}
				
				damagedTrinket = stack
				dragonScaleLimit = if (stack.itemDamage <= stack.maxDamage / 2) 1 else 2
				continue
			}
			
			return null
		}
		
		if (damagedTrinket == null || dragonScaleCount !in 1..dragonScaleLimit){
			return null
		}
		
		return RepairInfo(dragonScaleCount, damagedTrinket)
	}
}
