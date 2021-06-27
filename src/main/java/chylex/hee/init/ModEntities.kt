package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.entity.DefaultEntityAttributes
import chylex.hee.game.entity.ENTITY_GRAVITY
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
import chylex.hee.game.entity.set
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.with
import chylex.hee.network.data.ColorDataSerializer
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.named
import chylex.hee.system.forge.registerAllFields
import chylex.hee.system.migration.EntityEnderman
import chylex.hee.system.migration.EntityEndermite
import chylex.hee.system.migration.EntitySilverfish
import chylex.hee.system.reflection.ObjectConstructors
import chylex.hee.system.serialization.TagCompound
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityClassification.CREATURE
import net.minecraft.entity.EntityClassification.MISC
import net.minecraft.entity.EntityClassification.MONSTER
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.entity.EntityType.IFactory
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.Attributes.FLYING_SPEED
import net.minecraft.entity.ai.attributes.Attributes.FOLLOW_RANGE
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.entity.monster.MonsterEntity
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap.Type
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModEntities {
	@JvmField val TERRITORY_LIGHTNING_BOLT = build<EntityTerritoryLightningBolt>(MISC).size(0F, 0F).tracker(256, 3, false).disableSerialization().name("territory_lightning_bolt")
	
	@JvmField val ITEM_CAULDRON_TRIGGER         = build<EntityItemCauldronTrigger>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_cauldron_trigger")
	@JvmField val ITEM_FRESHLY_COOKED           = build<EntityItemFreshlyCooked>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_freshly_cooked")
	@JvmField val ITEM_IGNEOUS_ROCK             = build<EntityItemIgneousRock>(MISC).size(0.25F, 0.25F).immuneToFire().tracker(64, 3, true).name("item_igneous_rock")
	@JvmField val ITEM_NO_BOB                   = build<EntityItemNoBob>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_no_bob")
	@JvmField val ITEM_REVITALIZATION_SUBSTANCE = build<EntityItemRevitalizationSubstance>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_revitalization_substance")
	
	@JvmField val FALLING_BLOCK_HEAVY = build<EntityFallingBlockHeavy>(MISC).size(0.98F, 0.98F).tracker(160, 20, true).name("falling_block_heavy")
	@JvmField val FALLING_OBSIDIAN    = build<EntityFallingObsidian>(MISC).size(0.98F, 0.98F).tracker(160, 20, true).name("falling_obsidian")
	@JvmField val INFUSED_TNT         = build<EntityInfusedTNT>(MISC).size(0.98F, 0.98F).tracker(160, 10, true).name("infused_tnt")
	@JvmField val TOKEN_HOLDER        = build<EntityTokenHolder>(MISC).size(0.55F, 0.675F).tracker(128, 60, false).name("token_holder")
	
	@JvmField val ENDER_EYE = build<EntityBossEnderEye>(MONSTER).size(1.1F, 1F).immuneToFire().tracker(160, 1, true).name("ender_eye")
	
	@JvmField val ANGRY_ENDERMAN        = build<EntityMobAngryEnderman>(MONSTER).size(0.6F, 2.9F).tracker(80, 3, true).name("angry_enderman")
	@JvmField val BLOBBY                = build<EntityMobBlobby>(CREATURE).size(0.5F, 0.5F).tracker(80, 3, true).name("blobby")
	@JvmField val ENDERMAN              = build<EntityMobEnderman>(MONSTER).size(0.6F, 2.9F).tracker(80, 3, true).name("enderman")
	@JvmField val ENDERMAN_MUPPET       = build<EntityMobEndermanMuppet>(MISC).size(0.6F, 2.9F).tracker(96, 3, false).name("enderman_muppet")
	@JvmField val ENDERMITE             = build<EntityMobEndermite>(MONSTER).size(0.425F, 0.325F).tracker(80, 3, true).name("endermite")
	@JvmField val ENDERMITE_INSTABILITY = build<EntityMobEndermiteInstability>(MONSTER).size(0.425F, 0.325F).tracker(96, 3, true).name("endermite_instability")
	@JvmField val SILVERFISH            = build<EntityMobSilverfish>(MONSTER).size(0.575F, 0.35F).tracker(80, 3, true).name("silverfish")
	@JvmField val SPIDERLING            = build<EntityMobSpiderling>(MONSTER).size(0.675F, 0.45F).tracker(80, 2, true).name("spiderling")
	@JvmField val UNDREAD               = build<EntityMobUndread>(MONSTER).size(0.625F, 1.925F).tracker(80, 3, true).name("undread")
	@JvmField val VAMPIRE_BAT           = build<EntityMobVampireBat>(MONSTER).size(0.5F, 0.9F).tracker(80, 3, true).name("vampire_bat")
	@JvmField val VILLAGER_DYING        = build<EntityMobVillagerDying>(MISC).size(0.6F, 1.95F).tracker(80, 3, false).name("villager_dying")
	
	@JvmField val ENDER_PEARL       = build<EntityProjectileEnderPearl>(MISC).size(0.35F, 0.35F).tracker(64, 10, true).name("ender_pearl")
	@JvmField val EXPERIENCE_BOTTLE = build<EntityProjectileExperienceBottle>(MISC).size(0.25F, 0.25F).tracker(64, 10, false).name("experience_bottle")
	@JvmField val EYE_OF_ENDER      = build<EntityProjectileEyeOfEnder>(MISC).size(0.5F, 1F).tracker(64, 60, false).name("eye_of_ender")
	@JvmField val SPATIAL_DASH      = build<EntityProjectileSpatialDash>(MISC).size(0.2F, 0.2F).tracker(64, 10, true).name("spatial_dash")
	
	@JvmField val CAUSATUM_EVENT      = build<EntityTechnicalCausatumEvent>(MISC).size(0F, 0F).immuneToFire().tracker(1, Int.MAX_VALUE, false).name("causatum_event")
	@JvmField val IGNEOUS_PLATE_LOGIC = build<EntityTechnicalIgneousPlateLogic>(MISC).size(0F, 0F).tracker(32, 10, false).name("igneous_plate_logic")
	@JvmField val TECHNICAL_PUZZLE    = build<EntityTechnicalPuzzle>(MISC).size(0F, 0F).immuneToFire().tracker(1, Int.MAX_VALUE, false).name("technical_puzzle")
	@JvmField val TECHNICAL_TRIGGER   = build<EntityTechnicalTrigger>(MISC).size(0F, 0F).immuneToFire().tracker(256, Int.MAX_VALUE, false).name("technical_trigger")
	
	@SubscribeEvent
	fun onRegisterTypes(e: RegistryEvent.Register<EntityType<*>>) {
		e.registerAllFields(this)
		
		// data
		
		DataSerializers.registerSerializer(ColorDataSerializer)
		
		// spawns
		
		val defaultSpawnPredicateHostile = MonsterEntity::canMonsterSpawnInLight
		val defaultSpawnPredicatePassive = MobEntity::canSpawnOn
		
		EntitySpawnPlacementRegistry.register(ANGRY_ENDERMAN, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicateHostile)
		EntitySpawnPlacementRegistry.register(BLOBBY, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicatePassive)
		EntitySpawnPlacementRegistry.register(ENDERMAN, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, EntityMobEnderman.Companion::canSpawnAt)
		EntitySpawnPlacementRegistry.register(ENDERMITE, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicateHostile)
		EntitySpawnPlacementRegistry.register(ENDERMITE_INSTABILITY, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicateHostile)
		EntitySpawnPlacementRegistry.register(SILVERFISH, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicateHostile)
		EntitySpawnPlacementRegistry.register(SPIDERLING, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicateHostile)
		EntitySpawnPlacementRegistry.register(UNDREAD, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicateHostile)
		EntitySpawnPlacementRegistry.register(VAMPIRE_BAT, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicatePassive)
		
		replaceVanillaFactories()
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onRegisterAttributes(attributes: EntityAttributeCreationEvent) {
		attributes[ENDER_EYE] = DefaultEntityAttributes.hostileMob.with(
			MAX_HEALTH to 300.0,
			ATTACK_DAMAGE to 4.0,
			FLYING_SPEED to 0.093,
			FOLLOW_RANGE to 16.0,
		)
		
		attributes[ANGRY_ENDERMAN] = DefaultEntityAttributes.hostileMob.with(
			MAX_HEALTH to 40.0,
			ATTACK_DAMAGE to 7.0,
			MOVEMENT_SPEED to 0.315,
			FOLLOW_RANGE to 32.0,
		)
		
		attributes[BLOBBY] = DefaultEntityAttributes.peacefulMob.with(
			MAX_HEALTH to 8.0,
			MOVEMENT_SPEED to 0.19,
			FOLLOW_RANGE to 44.0,
			ENTITY_GRAVITY to ENTITY_GRAVITY.defaultValue * 0.725,
		)
		
		attributes[ENDERMAN] = DefaultEntityAttributes.hostileMob.with(
			MAX_HEALTH to 40.0,
			ATTACK_DAMAGE to 5.0,
			MOVEMENT_SPEED to 0.3,
			FOLLOW_RANGE to 64.0,
		)
		
		attributes[ENDERMAN_MUPPET] = DefaultEntityAttributes.hostileMob.with(
			MAX_HEALTH to 40.0,
			MOVEMENT_SPEED to 0.0,
		)
		
		attributes[ENDERMITE] = DefaultEntityAttributes.endermite.with(
			MAX_HEALTH to 8.0,
			ATTACK_DAMAGE to 2.0,
		)
		
		attributes[ENDERMITE_INSTABILITY] = DefaultEntityAttributes.endermite.with(
			MAX_HEALTH to 8.0,
			ATTACK_DAMAGE to 2.0,
		)
		
		attributes[SILVERFISH] = DefaultEntityAttributes.silverfish.with(
			MAX_HEALTH to 8.0,
			ATTACK_DAMAGE to 2.0,
			FOLLOW_RANGE to 12.0,
		)
		
		attributes[SPIDERLING] = DefaultEntityAttributes.hostileMob.with(
			ATTACK_DAMAGE to 1.5,
			MOVEMENT_SPEED to 0.32,
			FOLLOW_RANGE to 20.0,
		)
		
		attributes[UNDREAD] = DefaultEntityAttributes.hostileMob.with(
			MAX_HEALTH to 12.0,
			ATTACK_DAMAGE to 4.0,
			MOVEMENT_SPEED to 0.18,
			FOLLOW_RANGE to 24.0,
		)
		
		attributes[VAMPIRE_BAT] = DefaultEntityAttributes.peacefulMob.with(
			FOLLOW_RANGE to 14.5,
			ATTACK_DAMAGE to 0.0,
			FLYING_SPEED to 0.1
		)
		
		attributes[VILLAGER_DYING] = DefaultEntityAttributes.peacefulMob
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
			EntityEnderman::class.java   -> EntityMobEnderman(world)
			EntityEndermite::class.java  -> EntityMobEndermite(world)
			EntitySilverfish::class.java -> EntityMobSilverfish(world)
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
	private inline fun <reified T : Entity> build(classification: EntityClassification): EntityType.Builder<T> {
		val handle = ObjectConstructors.generic<T, Entity, IFactory<T>>("create", EntityType::class.java, World::class.java)
		return EntityType.Builder.create(handle.invokeExact() as IFactory<T>, classification)
	}
	
	private fun <T : Entity> EntityType.Builder<T>.tracker(trackingRange: Int, updateInterval: Int, receiveVelocityUpdates: Boolean): EntityType.Builder<T> {
		return this.setTrackingRange(trackingRange).setUpdateInterval(updateInterval).setShouldReceiveVelocityUpdates(receiveVelocityUpdates)
	}
	
	private fun <T : Entity> EntityType.Builder<T>.name(name: String): EntityType<T> {
		return this.build("hee.$name") named name
	}
}
