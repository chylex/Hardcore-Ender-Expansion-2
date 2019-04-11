package chylex.hee.system.capability
import chylex.hee.HEE
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
object PlayerCapabilityHandler{
	interface IPlayerCapability{
		val key: ResourceLocation
		fun provide(player: EntityPlayer): ICapabilityProvider
	}
	
	interface IPlayerPersistentCapability<T : INBTSerializable<NBTTagCompound>> : IPlayerCapability{
		fun retrieve(player: EntityPlayer): T
	}
	
	// Registry
	
	private val entries = mutableListOf<IPlayerCapability>()
	
	@JvmStatic
	fun register(capability: IPlayerCapability){
		entries.add(capability)
	}
	
	// Event handling
	
	@JvmStatic
	@SubscribeEvent
	fun onAttachCapabilities(e: AttachCapabilitiesEvent<Entity>){
		val entity = e.`object`
		
		if (entity is EntityPlayer){
			for(entry in entries){
				e.addCapability(entry.key, entry.provide(entity))
			}
		}
	}
	
	@JvmStatic
	@SubscribeEvent(priority = HIGHEST)
	fun onPlayerClone(e: PlayerEvent.Clone){
		val oldPlayer = e.original
		val newPlayer = e.entityPlayer
		
		for(entry in entries){
			if (entry is IPlayerPersistentCapability<*>){
				entry.retrieve(newPlayer).deserializeNBT(entry.retrieve(oldPlayer).serializeNBT())
			}
		}
	}
}
