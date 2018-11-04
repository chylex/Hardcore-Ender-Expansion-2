package chylex.hee.game.item
import chylex.hee.game.item.base.ItemBaseTrinket
import chylex.hee.game.mechanics.TrinketHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ItemScaleOfFreefall : ItemBaseTrinket(){
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
		
		if (entity is EntityPlayer && TrinketHandler.getCurrentActiveItem(entity, this) != null){
			e.amount *= 0.8F
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onLivingDeath(e: LivingDeathEvent){
		if (e.source !== DamageSource.FALL){
			return
		}
		
		val player = e.entity as? EntityPlayer ?: return
		val trinketItem = TrinketHandler.getCurrentActiveItem(player, this) ?: return
		
		player.health = 1F
		++trinketItem.itemDamage
		
		// TODO sound effect
		e.isCanceled = true
	}
}
