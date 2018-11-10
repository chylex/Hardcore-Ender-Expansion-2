package chylex.hee.game.item.base
import chylex.hee.game.item.util.CustomToolMaterial
import chylex.hee.game.render.util.RGB
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.stats.StatList
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AnvilUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.min

abstract class ItemBaseVoidTool : ItemTool(CustomToolMaterial.VOID, emptySet()){
	init{
		@Suppress("LeakingThis")
		setNoRepair()
		
		@Suppress("LeakingThis")
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean{
		return false
	}
	
	// Durability handling
	
	final override fun setDamage(stack: ItemStack, damage: Int){
		super.setDamage(stack, min(damage, stack.maxDamage))
	}
	
	protected fun guardItemBreaking(stack: ItemStack, entity: EntityLivingBase, block: () -> Unit){
		val wasNotBroken = stack.itemDamage < stack.maxDamage
		block()
		val isNowBroken = stack.itemDamage >= stack.maxDamage
		
		if (wasNotBroken && isNowBroken){
			entity.renderBrokenItemStack(stack)
			
			if (entity is EntityPlayer){
				entity.addStat(StatList.getObjectBreakStats(this)!!)
			}
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
			RGB(160).toInt()
		else
			super.getRGBDurabilityForDisplay(stack)
	}
	
	// Repair handling
	
	final override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return toRepair.isItemDamaged && repairWith.item === toolMaterial.repairItemStack.item
	}
	
	@SubscribeEvent
	fun onAnvilUpdate(e: AnvilUpdateEvent){
		val target = e.left
		val ingredient = e.right
		
		if (target.item === this && getIsRepairable(target, ingredient)){
			val totalCost = target.repairCost + 1
			
			if (totalCost >= 40){ // TODO check if player is in creative, but I can't even do that with the event... replace all this shit with ASM
				e.output = ItemStack.EMPTY
				e.cost = 0
			}
			else{
				e.output = target.copy().also { // TODO cannot repair & change name at the same time, but CBA
					it.itemDamage = 0
					it.repairCost++
				}
				
				e.cost = totalCost
				e.materialCost = 1
			}
		}
	}
}
