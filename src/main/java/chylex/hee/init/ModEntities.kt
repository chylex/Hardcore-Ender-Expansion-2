package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.entity.IHeeEntityType
import chylex.hee.game.entity.IHeeMobEntityType
import chylex.hee.game.entity.effect.EntityTerritoryLightningBolt
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.entity.item.EntityFallingObsidian
import chylex.hee.game.entity.item.EntityInfusedTNT
import chylex.hee.game.entity.item.EntityItemCauldronTrigger
import chylex.hee.game.entity.item.EntityItemFreshlyCooked
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.entity.item.EntityItemRevitalizationSubstance
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.EntityMobAngryEnderman
import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.entity.living.EntityMobEnderman
import chylex.hee.game.entity.living.EntityMobEndermanMuppet
import chylex.hee.game.entity.living.EntityMobEndermite
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.living.EntityMobVampireBat
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl
import chylex.hee.game.entity.projectile.EntityProjectileExperienceBottle
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.util.set
import chylex.hee.system.named
import chylex.hee.system.registerAllFields
import chylex.hee.util.color.ColorDataSerializer
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.lang.ObjectConstructors
import chylex.hee.util.nbt.TagCompound
import net.minecraft.entity.Entity
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.EntityType
import net.minecraft.entity.EntityType.IFactory
import net.minecraft.entity.MobEntity
import net.minecraft.entity.monster.EndermanEntity
import net.minecraft.entity.monster.EndermiteEntity
import net.minecraft.entity.monster.SilverfishEntity
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModEntities {
	private val allTypes = mutableMapOf<EntityType<*>, IHeeEntityType<*>>()
	private val mobTypes = mutableListOf<MobEntry<*>>()
	
	@JvmField val TERRITORY_LIGHTNING_BOLT = build(EntityTerritoryLightningBolt.Type) named "territory_lightning_bolt"
	
	@JvmField val ITEM_CAULDRON_TRIGGER         = build(EntityItemCauldronTrigger.TYPE) named "item_cauldron_trigger"
	@JvmField val ITEM_FRESHLY_COOKED           = build(EntityItemFreshlyCooked.TYPE) named "item_freshly_cooked"
	@JvmField val ITEM_IGNEOUS_ROCK             = build(EntityItemIgneousRock.Type) named "item_igneous_rock"
	@JvmField val ITEM_NO_BOB                   = build(EntityItemNoBob.TYPE) named "item_no_bob"
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE = build(EntityItemRevitalizationSubstance.TYPE) named "item_revitalization_substance"
	
	@JvmField val FALLING_BLOCK_HEAVY = build(EntityFallingBlockHeavy.TYPE) named "falling_block_heavy"
	@JvmField val FALLING_OBSIDIAN    = build(EntityFallingObsidian.TYPE) named "falling_obsidian"
	@JvmField val INFUSED_TNT         = build(EntityInfusedTNT.Type) named "infused_tnt"
	@JvmField val TOKEN_HOLDER        = build(EntityTokenHolder.Type) named "token_holder"
	
	@JvmField val ENDER_EYE = build(EntityBossEnderEye.Type) named "ender_eye"
	
	@JvmField val ANGRY_ENDERMAN        = build(EntityMobAngryEnderman.Type) named "angry_enderman"
	@JvmField val BLOBBY                = build(EntityMobBlobby.Type) named "blobby"
	@JvmField val ENDERMAN              = build(EntityMobEnderman.Type) named "enderman"
	@JvmField val ENDERMAN_MUPPET       = build(EntityMobEndermanMuppet.Type) named "enderman_muppet"
	@JvmField val ENDERMITE             = build(EntityMobEndermite.TYPE) named "endermite"
	@JvmField val ENDERMITE_INSTABILITY = build(EntityMobEndermiteInstability.Type) named "endermite_instability"
	@JvmField val SILVERFISH            = build(EntityMobSilverfish.Type) named "silverfish"
	@JvmField val SPIDERLING            = build(EntityMobSpiderling.Type) named "spiderling"
	@JvmField val UNDREAD               = build(EntityMobUndread.Type) named "undread"
	@JvmField val VAMPIRE_BAT           = build(EntityMobVampireBat.Type) named "vampire_bat"
	@JvmField val VILLAGER_DYING        = build(EntityMobVillagerDying.Type) named "villager_dying"
	
	@JvmField val ENDER_PEARL       = build(EntityProjectileEnderPearl.Type) named "ender_pearl"
	@JvmField val EXPERIENCE_BOTTLE = build(EntityProjectileExperienceBottle.Type) named "experience_bottle"
	@JvmField val EYE_OF_ENDER      = build(EntityProjectileEyeOfEnder.Type) named "eye_of_ender"
	@JvmField val SPATIAL_DASH      = build(EntityProjectileSpatialDash.Type) named "spatial_dash"
	
	@JvmField val CAUSATUM_EVENT      = build(EntityTechnicalCausatumEvent.TYPE) named "causatum_event"
	@JvmField val IGNEOUS_PLATE_LOGIC = build(EntityTechnicalIgneousPlateLogic.Type) named "igneous_plate_logic"
	@JvmField val TECHNICAL_PUZZLE    = build(EntityTechnicalPuzzle.TYPE) named "technical_puzzle"
	@JvmField val TECHNICAL_TRIGGER   = build(EntityTechnicalTrigger.Type) named "technical_trigger"
	
	private data class MobEntry<T : MobEntity>(val type: EntityType<T>, val properties: IHeeMobEntityType<T>) {
		fun registerPlacement() {
			val placement = properties.placement
			if (placement != null) {
				EntitySpawnPlacementRegistry.register(type, placement.placementType, placement.heightmapType, placement.predicate)
			}
		}
	}
	
	@SubscribeEvent
	fun onRegisterTypes(e: RegistryEvent.Register<EntityType<*>>) {
		e.registerAllFields(this)
		
		for (entry in mobTypes) {
			entry.registerPlacement()
		}
		
		DataSerializers.registerSerializer(ColorDataSerializer)
		
		replaceVanillaFactories()
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onRegisterAttributes(attributes: EntityAttributeCreationEvent) {
		for ((type, properties) in mobTypes) {
			attributes[type] = properties.attributes
		}
	}
	
	// Vanilla modifications
	
	private fun replaceVanillaFactories() {
		EntityType.ENDERMAN.factory = IFactory { _, world -> EntityMobEnderman(ENDERMAN, world) }
		EntityType.ENDERMITE.factory = IFactory { _, world -> EntityMobEndermite(ENDERMITE, world) }
		EntityType.SILVERFISH.factory = IFactory { _, world -> EntityMobSilverfish(SILVERFISH, world) }
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	fun onEntityJoinWorld(e: EntityJoinWorldEvent) {
		val world = e.world
		val original = e.entity
		
		// if a mod creates the entity manually instead of the factory, it must still be replaced
		val overriden = when (original.javaClass) {
			EndermanEntity::class.java   -> EntityMobEnderman(world)
			EndermiteEntity::class.java  -> EntityMobEndermite(world)
			SilverfishEntity::class.java -> EntityMobSilverfish(world)
			else                         -> return
		}
		
		// no way to handle passengers on the initial spawn
		overriden.read(original.writeWithoutTypeId(TagCompound()))
		
		e.isCanceled = true
		
		// should not happen, but avoids a crash if the entity was added during chunk loading, and for some
		// reason the entity was still the original type (for ex. if another mod messes with the factories)
		if (world.chunkProvider.isChunkLoaded(original)) {
			world.addEntity(overriden)
		}
	}
	
	// Utilities
	
	@Suppress("UNCHECKED_CAST")
	private inline fun <reified T : Entity, H : IHeeEntityType<T>> build(type: H): Pair<EntityType.Builder<T>, H> {
		val handle = ObjectConstructors.generic<T, Entity, IFactory<T>>("create", EntityType::class.java, World::class.java)
		var builder = EntityType.Builder.create(handle.invokeExact() as IFactory<T>, type.classification)
		
		builder = builder.size(type.size.width, type.size.height)
		builder = builder.setTrackingRange(type.tracker.trackingRange)
		builder = builder.setUpdateInterval(type.tracker.updateInterval)
		builder = builder.setShouldReceiveVelocityUpdates(type.tracker.receiveVelocityUpdates)
		
		if (type.isImmuneToFire) {
			builder = builder.immuneToFire()
		}
		
		if (type.disableSerialization) {
			builder = builder.disableSerialization()
		}
		
		return builder to type
	}
	
	private inline infix fun <reified T : Entity> Pair<EntityType.Builder<T>, IHeeEntityType<T>>.named(name: String): EntityType<T> {
		require(this.second !is IHeeMobEntityType<*>) // sanity check in case this gets called instead of the method below
		
		return first.build("hee.$name").also {
			allTypes[it] = this.second
		} named name
	}
	
	@JvmName("namedMob")
	private inline infix fun <reified T : MobEntity> Pair<EntityType.Builder<T>, IHeeMobEntityType<T>>.named(name: String): EntityType<T> {
		return first.build("hee.$name").also {
			allTypes[it] = this.second
			mobTypes.add(MobEntry(it, this.second))
		} named name
	}
}
