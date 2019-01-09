package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.entity.item.EntityFallingObsidian
import chylex.hee.game.entity.item.EntityInfusedTNT
import chylex.hee.game.entity.item.EntityItemFreshlyCooked
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.living.EntityMobEndermite
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.render.util.RGB
import chylex.hee.init.factory.EntityConstructors
import chylex.hee.system.Resource
import net.minecraft.entity.Entity
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.EntityEntry
import net.minecraftforge.fml.common.registry.EntityEntryBuilder
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import java.util.function.Function

@EventBusSubscriber(modid = HEE.ID)
object ModEntities{
	private var networkID = -1
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<EntityEntry>){
		networkID = 0
		
		with(e.registry){
			register<EntityItemNoBob>("item_no_bob").tracker(64, 3, true) to this
			register<EntityItemIgneousRock>("item_igneous_rock").tracker(64, 3, true) to this
			register<EntityItemFreshlyCooked>("item_freshly_cooked").tracker(64, 3, true) to this
			
			register<EntityFallingBlockHeavy>("falling_block_heavy").tracker(160, 20, true) to this
			register<EntityFallingObsidian>("falling_obsidian").tracker(160, 20, true) to this
			register<EntityInfusedTNT>("infused_tnt").tracker(160, 10, true) to this
			
			register<EntityMobEndermiteInstability>("endermite_instability").tracker(96, 3, true).egg(RGB(21u).toInt(), RGB(94, 122, 108).toInt()) to this
			
			register<EntityProjectileEnderPearl>("ender_pearl").tracker(64, 10, true) to this
			register<EntityProjectileEyeOfEnder>("eye_of_ender").tracker(64, 60, false) to this
			register<EntityProjectileSpatialDash>("projectile_spatial_dash").tracker(64, 10, true) to this
		}
		
		// vanilla modifications
		
		override<EntityMobEndermite>("endermite")
		override<EntityMobSilverfish>("silverfish")
	}
	
	// Utilities
	
	private inline fun <reified T : Entity> register(registryName: String): EntityEntryBuilder<T>{
		return EntityEntryBuilder
			.create<T>()
			.entity(T::class.java)
			.factory(EntityConstructors.get(T::class.java))
			.id(Resource.Custom(registryName), networkID++)
			.name("hee.$registryName")
	}
	
	@Suppress("NOTHING_TO_INLINE")
	private inline infix fun <T : Entity> EntityEntryBuilder<T>.to(registry: IForgeRegistry<EntityEntry>){
		registry.register(this.build())
	}
	
	private fun override(vanillaName: String, mobClass: Class<out Entity>){
		ForgeRegistries.ENTITIES.getValue(Resource.Vanilla(vanillaName))!!.apply { // TODO is there a better way?
			val entryFields = this::class.java.declaredFields
			
			entryFields.first { it.type === Class::class.java }.also { it.isAccessible = true }.set(this, mobClass)
			entryFields.first { it.type === Function::class.java }.also { it.isAccessible = true }.set(this, EntityConstructors.get(mobClass))
		}
	}
	
	private inline fun <reified T : Entity> override(vanillaName: String){
		override(vanillaName, T::class.java)
	}
}
