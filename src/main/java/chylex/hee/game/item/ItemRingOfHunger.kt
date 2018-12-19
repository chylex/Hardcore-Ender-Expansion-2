package chylex.hee.game.item
import chylex.hee.game.mechanics.TrinketHandler
import chylex.hee.game.mechanics.potion.PotionBase
import chylex.hee.system.util.ceilToInt
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemFood
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent
import net.minecraftforge.fml.relauncher.Side
import java.util.UUID
import kotlin.math.min

class ItemRingOfHunger : ItemAbstractTrinket(){
	private val foodUseTracker = mutableMapOf<UUID, Map<Potion, PotionEffect>>()
	
	init{
		maxStackSize = 1
		maxDamage = 120
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onPlayerTick(e: PlayerTickEvent){
		if (e.side == Side.SERVER && e.phase == Phase.START && e.player.ticksExisted % 12 == 0){
			val foodStats = e.player.foodStats
			
			if (foodStats.needFood()){
				TrinketHandler.get(e.player).transformIfActive(this){
					val chargePercentage = 1F - (it.itemDamage.toFloat() / it.maxDamage)
					val restoreHungerTo = min(20, 15 + (7F * chargePercentage).ceilToInt())
					
					if (foodStats.foodLevel < restoreHungerTo && chargePercentage > 0F){
						++it.itemDamage
						++foodStats.foodLevel
						
						if (foodStats.foodSaturationLevel < 1F){
							foodStats.foodSaturationLevel = 1F
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent(priority = LOWEST)
	fun onLivingUseItemTick(e: LivingEntityUseItemEvent.Tick){
		val player = e.entity as? EntityPlayer ?: return
		
		if (!player.world.isRemote && e.item.item is ItemFood && e.duration <= 1){
			foodUseTracker[player.uniqueID] = player.activePotionMap.mapValues { (type, effect) -> PotionEffect(type, effect.duration, effect.amplifier) }
		}
	}
	
	@SubscribeEvent
	fun onLivingUseItemFinish(e: LivingEntityUseItemEvent.Finish){
		val prevEffects = foodUseTracker.remove(e.entity.uniqueID) ?: return
		
		TrinketHandler.get(e.entity as EntityPlayer).transformIfActive(this){
			var restoredDurability = 0F
			
			for((type, effect) in e.entityLiving.activePotionMap){
				if (type.isBadEffect){
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
			
			it.itemDamage -= restoredDurability.ceilToInt()
		}
	}
}
