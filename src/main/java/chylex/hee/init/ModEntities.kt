package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.init.factory.EntityConstructors
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.EntityEntry
import net.minecraftforge.fml.common.registry.EntityEntryBuilder
import net.minecraftforge.registries.IForgeRegistry

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModEntities{
	private var networkID = -1
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<EntityEntry>){
		networkID = 0
		
		with(e.registry){
			register<EntityItemIgneousRock>("ItemIgneousRock").tracker(64, 3, true) to this
		}
	}
	
	// Utilities
	
	private inline fun <reified T : Entity> register(registryName: String): EntityEntryBuilder<T>{
		return EntityEntryBuilder
			.create<T>()
			.entity(T::class.java)
			.factory(EntityConstructors.get(T::class.java))
			.id(ResourceLocation(HardcoreEnderExpansion.ID, registryName), networkID++)
			.name("entity.hee.$registryName")
	}
	
	@Suppress("NOTHING_TO_INLINE")
	private inline infix fun <T : Entity> EntityEntryBuilder<T>.to(registry: IForgeRegistry<EntityEntry>){
		registry.register(this.build())
	}
}
