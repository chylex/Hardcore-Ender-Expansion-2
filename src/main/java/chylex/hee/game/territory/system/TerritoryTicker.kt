package chylex.hee.game.territory.system

import chylex.hee.HEE
import chylex.hee.game.territory.system.storage.TerritoryGlobalStorage
import chylex.hee.game.world.isEndDimension
import chylex.hee.network.client.PacketClientTerritoryEnvironment
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import net.minecraftforge.event.world.WorldEvent
import java.util.UUID

@SubscribeAllEvents(modid = HEE.ID)
object TerritoryTicker {
	private val active = mutableMapOf<TerritoryInstance, Entry>()
	private val lastTerritory = mutableMapOf<UUID, TerritoryInstance>()
	
	private class Entry(val behaviors: List<ITerritoryBehavior>) {
		var lastTickTime = -1L
	}
	
	fun onWorldTick(world: ServerWorld) {
		val currentTime = world.gameTime
		
		if (currentTime % 600L == 0L) {
			active.values.removeAll { currentTime - it.lastTickTime > 1L }
		}
		
		for (player in world.players) {
			val instance = TerritoryInstance.fromPos(player)
			if (instance == null) {
				lastTerritory.remove(player.uniqueID)
				continue
			}
			
			val storage = TerritoryGlobalStorage.get().forInstance(instance)
			if (storage == null) {
				continue
			}
			
			val entry = active.getOrPut(instance) {
				Entry(mutableListOf<ITerritoryBehavior>().also {
					instance.territory.desc.initialize(instance, storage, it)
				})
			}
			
			if (entry.lastTickTime != currentTime) {
				entry.lastTickTime = currentTime
				entry.behaviors.forEach { it.tick(world) }
			}
			
			if (instance != lastTerritory.put(player.uniqueID, instance) || entry.behaviors.any { it.resendClientEnvironmentPacketOnWorldTick == currentTime }) {
				PacketClientTerritoryEnvironment(storage).sendToPlayer(player)
			}
		}
	}
	
	@SubscribeEvent
	fun onWorldSave(e: WorldEvent.Save) {
		if (e.world.let { it is World && it.isEndDimension }) {
			active.clear()
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onPlayerChangedDimension(e: PlayerChangedDimensionEvent) {
		if (e.to !== HEE.dim) {
			lastTerritory.remove(e.player.uniqueID)
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onPlayerLoggedOut(e: PlayerLoggedOutEvent) {
		lastTerritory.remove(e.player.uniqueID)
	}
}
