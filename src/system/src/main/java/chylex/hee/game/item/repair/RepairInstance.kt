package chylex.hee.game.item.repair

import chylex.hee.game.inventory.size
import chylex.hee.system.math.ceilToInt
import net.minecraft.item.ItemStack

class RepairInstance(val target: ItemStack, val ingredient: ItemStack) {
	var repaired: ItemStack = ItemStack.EMPTY
	
	var repairCost = target.repairCost
	var experienceCost = 0
	var ingredientCost = 0
	
	fun repairFully() {
		repaired = target.copy().also { it.damage = 0 }
		experienceCost = target.repairCost + 1
		ingredientCost = 1
	}
	
	fun repairPercent(percent: Int) {
		val damagePerIngredient = (target.maxDamage * (percent / 100F)).ceilToInt()
		
		repaired = target.copy()
		experienceCost = target.repairCost
		ingredientCost = 0
		
		for(step in 0 until ingredient.size) {
			repaired.damage -= damagePerIngredient
			experienceCost++
			ingredientCost++
			
			if (repaired.damage == 0) {
				break
			}
		}
	}
}
