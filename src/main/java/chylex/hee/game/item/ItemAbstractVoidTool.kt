package chylex.hee.game.item
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.repair.RepairHandler
import chylex.hee.game.item.repair.RepairInstance
import chylex.hee.game.item.util.CustomToolMaterial
import chylex.hee.system.util.color.RGB
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.stats.StatList
import kotlin.math.min

abstract class ItemAbstractVoidTool : ItemTool(CustomToolMaterial.VOID, emptySet()), ICustomRepairBehavior{
	init{
		@Suppress("LeakingThis")
		setNoRepair()
	}
	
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean{
		return false
	}
	
	// Durability handling
	
	final override fun setDamage(stack: ItemStack, damage: Int){
		super.setDamage(stack, min(damage, stack.maxDamage))
	}
	
	protected inline fun guardItemBreaking(stack: ItemStack, entity: EntityLivingBase, block: () -> Unit){
		val wasNotBroken = stack.itemDamage < stack.maxDamage
		block()
		val isNowBroken = stack.itemDamage >= stack.maxDamage
		
		if (wasNotBroken && isNowBroken){
			onItemBroken(stack, entity)
		}
	}
	
	protected fun onItemBroken(stack: ItemStack, entity: EntityLivingBase){
		entity.renderBrokenItemStack(stack)
		
		if (entity is EntityPlayer){
			entity.addStat(StatList.getObjectBreakStats(this)!!)
		}
	}
	
	override fun getDurabilityForDisplay(stack: ItemStack): Double{
		return if (stack.itemDamage >= stack.maxDamage)
			0.0
		else
			super.getDurabilityForDisplay(stack)
	}
	
	override fun getRGBDurabilityForDisplay(stack: ItemStack): Int{
		return if (stack.itemDamage >= stack.maxDamage)
			RGB(160u).toInt()
		else
			super.getRGBDurabilityForDisplay(stack)
	}
	
	// Repair handling
	
	final override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return toRepair.isItemDamaged && repairWith.item === toolMaterial.repairItemStack.item
	}
	
	final override fun onRepairUpdate(instance: RepairInstance) = with(instance){
		repairFully()
		repairCost = min(repairCost + 1, RepairHandler.MAX_EXPERIENCE_COST)
	}
}
