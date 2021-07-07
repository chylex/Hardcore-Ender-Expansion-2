package chylex.hee.game.entity.player

import chylex.hee.HEE
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.nbt.TagCompound
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent

@SubscribeAllEvents(modid = HEE.ID)
object PlayerCapabilityHandler {
	interface IPlayerCapability {
		val key: ResourceLocation
		fun provide(player: PlayerEntity): ICapabilityProvider
	}
	
	interface IPlayerPersistentCapability<T : INBTSerializable<TagCompound>> : IPlayerCapability {
		fun retrieve(player: PlayerEntity): T
	}
	
	// Registry
	
	private val entries = mutableListOf<IPlayerCapability>()
	
	fun register(capability: IPlayerCapability) {
		entries.add(capability)
	}
	
	// Event handling
	
	@SubscribeEvent
	fun onAttachCapabilities(e: AttachCapabilitiesEvent<Entity>) {
		val entity = e.`object`
		
		if (entity is PlayerEntity) {
			for (entry in entries) {
				e.addCapability(entry.key, entry.provide(entity))
			}
		}
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerClone(e: PlayerEvent.Clone) {
		val oldPlayer = e.original
		val newPlayer = e.player
		
		for (entry in entries) {
			if (entry is IPlayerPersistentCapability<*>) {
				entry.retrieve(newPlayer).deserializeNBT(entry.retrieve(oldPlayer).serializeNBT())
			}
		}
	}
}
