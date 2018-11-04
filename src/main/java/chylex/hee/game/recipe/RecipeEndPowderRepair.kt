package chylex.hee.game.recipe
import chylex.hee.init.ModItems
import chylex.hee.system.util.enchantmentList
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.shlong
import chylex.hee.system.util.size
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import java.util.Objects
import java.util.Random

object RecipeEndPowderRepair : RecipeBaseDynamic(){
	private val ENCHANTMENT_COMPARATOR: Comparator<Pair<Enchantment, Int>> = compareBy({ it.first.registryName }, { it.second })
	
	override fun canFit(width: Int, height: Int): Boolean{
		return (width * height) > 2
	}
	
	override fun matches(inv: InventoryCrafting, world: World): Boolean{
		return determineRepairInfo(inv) != null
	}
	
	override fun getCraftingResult(inv: InventoryCrafting): ItemStack{
		val (endPowderCount, repairItem1, repairItem2) = determineRepairInfo(inv) ?: return ItemStack.EMPTY
		
		val item = repairItem1.item
		val durability = repairItem1.maxDamage
		
		val item1Durability = durability - repairItem1.itemDamage
		val item2Durability = durability - repairItem2.itemDamage
		
		val newDurability = item1Durability + item2Durability + (durability / 20) + (durability * endPowderCount / 10)
		val repairedStack = ItemStack(item, 1, durability - newDurability) // itemDamage < 0 is handled in Item.setDamage
		
		val allEnchantments = (repairItem1.enchantmentList + repairItem2.enchantmentList).sortedWith(ENCHANTMENT_COMPARATOR).toList()
		
		if (allEnchantments.isNotEmpty()){
			val seed = (item.registryName!!.hashCode() shlong 32) + allEnchantments.fold(1){ hash, (enchantment, level) -> Objects.hash(hash, enchantment.registryName, level) }
			
			val (enchantment, level) = Random(seed).nextItem(allEnchantments)!!
			repairedStack.addEnchantment(enchantment, level)
		}
		
		return repairedStack
	}
	
	// Repair info
	
	private data class RepairInfo(val endPowderCount: Int, val repairItem1: ItemStack, val repairItem2: ItemStack)
	
	private fun determineRepairInfo(inv: InventoryCrafting): RepairInfo?{
		var endPowderCount = 0
		var repairItem1: ItemStack? = null
		var repairItem2: ItemStack? = null
		
		for((_, stack) in inv.nonEmptySlots){
			if (stack.item === ModItems.END_POWDER){
				++endPowderCount
				continue
			}
			
			if (stack.size != 1){
				return null
			}
			
			if (repairItem1 == null){
				repairItem1 = stack
			}
			else if (repairItem2 == null && canRepairTogether(repairItem1, stack)){
				repairItem2 = stack
			}
			else{
				return null
			}
		}
		
		if (repairItem1 == null || repairItem2 == null){
			return null
		}
		
		return RepairInfo(endPowderCount, repairItem1, repairItem2)
	}
	
	private fun canRepairTogether(repairItem1: ItemStack, repairItem2: ItemStack): Boolean{
		return repairItem1.item.let { it === repairItem2.item && it.isRepairable } && repairItem1.maxDamage == repairItem2.maxDamage
	}
}
