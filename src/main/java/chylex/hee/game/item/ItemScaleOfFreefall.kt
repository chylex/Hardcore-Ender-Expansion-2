package chylex.hee.game.item

import chylex.hee.game.entity.posVec
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.world.playClient
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientTrinketBreak
import chylex.hee.system.compatibility.MinecraftForgeEventBus
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent

class ItemScaleOfFreefall(properties: Properties) : ItemAbstractTrinket(properties) {
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean {
		return stack.damage < stack.maxDamage
	}
	
	@Sided(Side.CLIENT)
	override fun spawnClientTrinketBreakFX(target: Entity) {
		ModSounds.ENTITY_PLAYER_DEATH_NO_SUBTITLES.playClient(target.posVec, target.soundCategory)
		ModSounds.ITEM_SCALE_OF_FREEFALL_USE.playClient(target.posVec, target.soundCategory, volume = 0.6F)
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onLivingDamage(e: LivingDamageEvent) {
		if (e.source !== DamageSource.FALL) {
			return
		}
		
		val entity = e.entity
		
		if (entity is EntityPlayer && TrinketHandler.get(entity).isItemActive(this)) {
			e.amount *= 0.8F
		}
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onLivingDeath(e: LivingDeathEvent) {
		if (e.source !== DamageSource.FALL) {
			return
		}
		
		val player = e.entity as? EntityPlayer ?: return
		
		TrinketHandler.get(player).transformIfActive(this) {
			++it.damage
			
			player.health = 1F
			e.isCanceled = true
			
			PacketClientTrinketBreak(player, item).sendToAllAround(player, 24.0)
		}
	}
}
