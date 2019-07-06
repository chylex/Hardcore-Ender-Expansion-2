package chylex.hee.game.item
import chylex.hee.game.mechanics.trinket.TrinketHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ItemScaleOfFreefall : ItemAbstractTrinket(){
	init{
		maxDamage = 8
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean{
		return stack.itemDamage < stack.maxDamage
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onLivingDamage(e: LivingDamageEvent){
		if (e.source !== DamageSource.FALL){
			return
		}
		
		val entity = e.entity
		
		if (entity is EntityPlayer && TrinketHandler.get(entity).isItemActive(this)){
			e.amount *= 0.8F
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onLivingDeath(e: LivingDeathEvent){
		if (e.source !== DamageSource.FALL){
			return
		}
		
		val player = e.entity as? EntityPlayer ?: return
		
		TrinketHandler.get(player).transformIfActive(this){
			++it.itemDamage
			
			player.health = 1F
			e.isCanceled = true
			
			// TODO sound effect
		}
	}
}
