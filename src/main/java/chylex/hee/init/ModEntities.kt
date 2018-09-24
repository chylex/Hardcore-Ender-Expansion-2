package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.init.factory.EntityConstructors
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.EntityEntry
import net.minecraftforge.fml.common.registry.EntityEntryBuilder
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import java.util.function.Function

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModEntities{
	private var networkID = -1
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<EntityEntry>){
		networkID = 0
		
		with(e.registry){
			register<EntityItemNoBob>("item_no_bob").tracker(64, 3, true) to this
			register<EntityItemIgneousRock>("item_igneous_rock").tracker(64, 3, true) to this
			
			register<EntityProjectileSpatialDash>("projectile_spatial_dash").tracker(64, 10, true) to this
		}
		
		// vanilla modifications
		
		ForgeRegistries.ENTITIES.getValue(ResourceLocation("minecraft", "silverfish"))!!.apply { // TODO is there a better way?
			val mobClass = EntityMobSilverfish::class.java
			val entryFields = this::class.java.declaredFields
			
			entryFields.first { it.type == Class::class.java }.also { it.isAccessible = true }.set(this, mobClass)
			entryFields.first { it.type == Function::class.java }.also { it.isAccessible = true }.set(this, EntityConstructors.get(mobClass))
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
