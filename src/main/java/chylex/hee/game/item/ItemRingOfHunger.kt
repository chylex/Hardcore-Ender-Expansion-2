package chylex.hee.game.item
import chylex.hee.game.mechanics.potion.PotionBase
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Potion
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.makeEffect
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent.Phase
import net.minecraftforge.event.TickEvent.PlayerTickEvent
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.fml.LogicalSide
import java.util.UUID
import kotlin.math.min

class ItemRingOfHunger(properties: Properties) : ItemAbstractTrinket(properties){
	private val foodUseTracker = mutableMapOf<UUID, Map<Potion, EffectInstance>>()
	
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onPlayerTick(e: PlayerTickEvent){
		if (e.side == LogicalSide.SERVER && e.phase == Phase.START && e.player.ticksExisted % 12 == 0){
			val player = e.player
			val foodStats = player.foodStats
			
			if (foodStats.needFood()){
				TrinketHandler.get(player).transformIfActive(this){
					val chargePercentage = 1F - (it.damage.toFloat() / it.maxDamage)
					val restoreHungerTo = min(20, (14.6F + (8.1F * chargePercentage)).ceilToInt())
					
					if (foodStats.foodLevel < restoreHungerTo && chargePercentage > 0F){
						++it.damage
						++foodStats.foodLevel
						
						if (foodStats.foodSaturationLevel < 1F){
							foodStats.foodSaturationLevel = 1F
						}
						
						if (it.damage >= it.maxDamage){
							TrinketHandler.playTrinketBreakFX(player, this)
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingUseItemTick(e: LivingEntityUseItemEvent.Tick){
		val player = e.entity as? EntityPlayer ?: return
		
		if (!player.world.isRemote && e.item.item.isFood && e.duration <= 1){
			foodUseTracker[player.uniqueID] = player.activePotionMap.mapValues { (type, effect) -> type.makeEffect(effect.duration, effect.amplifier) }
		}
	}
	
	@SubscribeEvent
	fun onLivingUseItemFinish(e: LivingEntityUseItemEvent.Finish){
		val prevEffects = foodUseTracker.remove(e.entity.uniqueID) ?: return
		
		TrinketHandler.get(e.entity as EntityPlayer).transformIfActive(this){
			var restoredDurability = 0F
			
			for((type, effect) in e.entityLiving.activePotionMap){
				if (type.effectType == HARMFUL){ // UPDATE consider neutral?
					if (effect.duration >= PotionBase.INFINITE_DURATION_THRESHOLD){
						restoredDurability = it.maxDamage.toFloat()
						break
					}
					else{
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
