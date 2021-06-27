package chylex.hee.game.world.territory.tickers

import chylex.hee.HEE
import chylex.hee.game.entity.isInEndDimension
import chylex.hee.game.entity.posVec
import chylex.hee.game.world.spawn
import chylex.hee.game.world.territory.ITerritoryTicker
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryVoid
import chylex.hee.game.world.territory.storage.data.VoidData
import chylex.hee.game.world.totalTime
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.EntityType
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.entity.living.LivingDeathEvent

class VoidTicker(private val data: VoidData) : ITerritoryTicker {
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		@SubscribeEvent(EventPriority.LOWEST)
		fun onLivingDeath(e: LivingDeathEvent) {
			val player = (e.entity as? EntityPlayer)?.takeIf { !it.world.isRemote && it.isInEndDimension } ?: return
			val instance = TerritoryInstance.fromPos(player) ?: return
			val voidData = instance.getStorageComponent<VoidData>() ?: return
			
			if (voidData.startCorrupting()) {
				player.world.spawn(EntityType.LIGHTNING_BOLT, player.posVec) { setEffectOnly(true) }
			}
		}
	}
	
	override var resendClientEnvironmentPacketOnWorldTick = Long.MIN_VALUE
	
	override fun tick(world: ServerWorld) {
		if (!data.isCorrupting || data.voidFactor >= TerritoryVoid.RARE_TERRITORY_MAX_CORRUPTION_FACTOR) {
			return
		}
		
		val currentTime = world.totalTime
		
		data.onCorruptionTick(currentTime)
		
		if (currentTime % 10L == 0L) {
			resendClientEnvironmentPacketOnWorldTick = currentTime
		}
	}
}
