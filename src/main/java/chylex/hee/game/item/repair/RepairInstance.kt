package chylex.hee.game.item.repair
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.size
import net.minecraft.item.ItemStack

class RepairInstance(val target: ItemStack, val ingredient: ItemStack){
	var repaired: ItemStack = ItemStack.EMPTY
	
	var repairCost = target.repairCost
	var experienceCost = 0
	var ingredientCost = 0
	
	fun repairFully(){
		repaired = target.copy().also { it.itemDamage = 0 }
		experienceCost = target.repairCost + 1
		ingredientCost = 1
	}
	
	fun repairPercent(percent: Int){
		val damagePerIngredient = (target.maxDamage * (percent / 100F)).ceilToInt()
		
		repaired = target.copy()
		experienceCost = target.repairCost
		ingredientCost = 0
		
		for(step in 0 until ingredient.size){
			repaired.itemDamage -= damagePerIngredient
			experienceCost++
			ingredientCost++
			
			if (repaired.itemDamage == 0){
				break
			}
		}
	}
}
