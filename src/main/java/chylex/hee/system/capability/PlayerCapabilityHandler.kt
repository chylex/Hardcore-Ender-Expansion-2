package chylex.hee.system.capability
import chylex.hee.HEE
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.TagCompound
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent

@SubscribeAllEvents(modid = HEE.ID)
object PlayerCapabilityHandler{
	interface IPlayerCapability{
		val key: ResourceLocation
		fun provide(player: EntityPlayer): ICapabilityProvider
	}
	
	interface IPlayerPersistentCapability<T : INBTSerializable<TagCompound>> : IPlayerCapability{
		fun retrieve(player: EntityPlayer): T
	}
	
	// Registry
	
	private val entries = mutableListOf<IPlayerCapability>()
	
	fun register(capability: IPlayerCapability){
		entries.add(capability)
	}
	
	// Event handling
	
	@SubscribeEvent
	fun onAttachCapabilities(e: AttachCapabilitiesEvent<Entity>){
		val entity = e.`object`
		
		if (entity is EntityPlayer){
			for(entry in entries){
				e.addCapability(entry.key, entry.provide(entity))
			}
		}
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerClone(e: PlayerEvent.Clone){
		val oldPlayer = e.original
		val newPlayer = e.player
		
		for(entry in entries){
			if (entry is IPlayerPersistentCapability<*>){
				entry.retrieve(newPlayer).deserializeNBT(entry.retrieve(oldPlayer).serializeNBT())
			}
		}
	}
}
