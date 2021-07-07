package chylex.hee.game.item

import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.potion.brewing.PotionBrewing.INFINITE_DURATION_THRESHOLD
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.system.MinecraftForgeEventBus
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.ceilToInt
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.potion.Effect
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraftforge.event.TickEvent.Phase
import net.minecraftforge.event.TickEvent.PlayerTickEvent
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.fml.LogicalSide
import java.util.UUID
import kotlin.math.min

class ItemRingOfHunger(properties: Properties) : ItemAbstractTrinket(properties) {
	private val foodUseTracker = mutableMapOf<UUID, Map<Effect, EffectInstance>>()
	
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	@SubscribeEvent
	fun onPlayerTick(e: PlayerTickEvent) {
		if (e.side == LogicalSide.SERVER && e.phase == Phase.START && e.player.ticksExisted % 12 == 0) {
			val player = e.player
			val foodStats = player.foodStats
			
			if (foodStats.needFood()) {
				TrinketHandler.get(player).transformIfActive(this) {
					val chargePercentage = 1F - (it.damage.toFloat() / it.maxDamage)
					val restoreHungerTo = min(20, (14.6F + (8.1F * chargePercentage)).ceilToInt())
					
					if (foodStats.foodLevel < restoreHungerTo && chargePercentage > 0F) {
						++it.damage
						++foodStats.foodLevel
						
						if (foodStats.foodSaturationLevel < 1F) {
							foodStats.foodSaturationLevel = 1F
						}
						
						if (it.damage >= it.maxDamage) {
							TrinketHandler.playTrinketBreakFX(player, this)
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingUseItemTick(e: LivingEntityUseItemEvent.Tick) {
		val player = e.entity as? PlayerEntity ?: return
		
		if (!player.world.isRemote && e.item.item.isFood && e.duration <= 1) {
			foodUseTracker[player.uniqueID] = player.activePotionMap.mapValues { (type, effect) -> type.makeInstance(effect.duration, effect.amplifier) }
		}
	}
	
	@SubscribeEvent
	fun onLivingUseItemFinish(e: LivingEntityUseItemEvent.Finish) {
		val prevEffects = foodUseTracker.remove(e.entity.uniqueID) ?: return
		
		TrinketHandler.get(e.entity as PlayerEntity).transformIfActive(this) {
			var restoredDurability = 0F
			
			for ((type, effect) in e.entityLiving.activePotionMap) {
				if (type.effectType == HARMFUL) {
					if (effect.duration >= INFINITE_DURATION_THRESHOLD) {
						restoredDurability = it.maxDamage.toFloat()
						break
					}
					else {
						val prevEffect = prevEffects[type]?.takeUnless { prev -> prev.amplifier < effect.amplifier }
						val prevDuration = prevEffect?.duration ?: 0
						
						restoredDurability += (1 + effect.amplifier) * (effect.duration - prevDuration) / 40F
					}
				}
			}
			
			it.damage -= restoredDurability.ceilToInt()
		}
	}
}
