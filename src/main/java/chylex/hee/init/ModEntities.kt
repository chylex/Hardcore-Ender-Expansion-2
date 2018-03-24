package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import net.minecraft.entity.Entity
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.EntityEntry
import net.minecraftforge.fml.common.registry.EntityEntryBuilder
import net.minecraftforge.registries.IForgeRegistry

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModEntities{
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<EntityEntry>){
		var id = 0
		
		with(e.registry){
			// TODO register<EntityChicken>(++id, "Test").to(this)
		}
	}
	
	// Utilities
	
	private inline fun <reified T: Entity> register(entityId: Int, registryName: String): EntityEntryBuilder<T>{
		return EntityEntryBuilder
			.create<T>()
			.entity(T::class.java)
			.id("${HardcoreEnderExpansion.ID}:$registryName", entityId)
			.name("entity.hee.$registryName")
	}
	
	@Suppress("NOTHING_TO_INLINE")
	private inline fun <T: Entity> EntityEntryBuilder<T>.to(registry: IForgeRegistry<EntityEntry>){
		registry.register(this.build())
	}
}
