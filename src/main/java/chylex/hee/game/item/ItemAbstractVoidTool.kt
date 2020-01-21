package chylex.hee.game.item
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.repair.RepairHandler
import chylex.hee.game.item.repair.RepairInstance
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.ItemTool
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.size
import net.minecraft.item.IItemTier
import net.minecraft.item.ItemStack
import kotlin.math.min

abstract class ItemAbstractVoidTool(properties: Properties, tier: IItemTier) : ItemTool(0F, -2.8F, tier, emptySet(), properties), ICustomRepairBehavior{
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean{
		return false
	}
	
	// Durability handling
	
	final override fun setDamage(stack: ItemStack, damage: Int){
		super.setDamage(stack, min(damage, stack.maxDamage))
	}
	
	protected inline fun guardItemBreaking(stack: ItemStack, block: () -> Unit){ // damage shrinks the stack and resets damage
		if (stack.damage >= stack.maxDamage){
			return
		}
		
		block()
		
		if (stack.isEmpty){
			stack.size += 1
			stack.damage = stack.maxDamage
		}
	}
	
	override fun getDurabilityForDisplay(stack: ItemStack): Double{
		return if (stack.damage >= stack.maxDamage)
			0.0
		else
			super.getDurabilityForDisplay(stack)
	}
	
	override fun getRGBDurabilityForDisplay(stack: ItemStack): Int{
		return if (stack.damage >= stack.maxDamage)
			RGB(160u).i
		else
			super.getRGBDurabilityForDisplay(stack)
	}
	
	// Repair handling
	
	final override fun onRepairUpdate(instance: RepairInstance) = with(instance){
		repairFully()
		repairCost = min(repairCost + 1, RepairHandler.MAX_EXPERIENCE_COST)
	}
}
