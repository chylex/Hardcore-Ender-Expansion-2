package chylex.hee.game.territory.behavior

import chylex.hee.HEE
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.territory.TerritoryVoid
import chylex.hee.game.territory.storage.VoidData
import chylex.hee.game.territory.system.ITerritoryBehavior
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.isInEndDimension
import chylex.hee.game.world.util.spawn
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.entity.living.LivingDeathEvent

class VoidCorruptionBehavior(private val data: VoidData) : ITerritoryBehavior {
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		@SubscribeEvent(EventPriority.LOWEST)
		fun onLivingDeath(e: LivingDeathEvent) {
			val player = (e.entity as? PlayerEntity)?.takeIf { !it.world.isRemote && it.isInEndDimension } ?: return
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
		
		val currentTime = world.gameTime
		
		data.onCorruptionTick(currentTime)
		
		if (currentTime % 10L == 0L) {
			resendClientEnvironmentPacketOnWorldTick = currentTime
		}
	}
}
