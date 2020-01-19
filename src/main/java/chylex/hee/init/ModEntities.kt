package chylex.hee.init
import chylex.hee.HEE
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
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.init.factory.EntityConstructors
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityEnderman
import chylex.hee.system.migration.vanilla.EntityEndermite
import chylex.hee.system.migration.vanilla.EntitySilverfish
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.named
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityClassification.MISC
import net.minecraft.entity.EntityClassification.MONSTER
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.entity.MobEntity
import net.minecraft.entity.monster.MonsterEntity
import net.minecraft.world.gen.Heightmap.Type
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModEntities{
	val ITEM_CAULDRON_TRIGGER         = build<EntityItemCauldronTrigger>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_cauldron_trigger")
	val ITEM_FRESHLY_COOKED           = build<EntityItemFreshlyCooked>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_freshly_cooked")
	val ITEM_IGNEOUS_ROCK             = build<EntityItemIgneousRock>(MISC).size(0.25F, 0.25F).immuneToFire().tracker(64, 3, true).name("item_igneous_rock")
	val ITEM_NO_BOB                   = build<EntityItemNoBob>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_no_bob")
	val ITEM_REVITALIZATION_SUBSTANCE = build<EntityItemRevitalizationSubstance>(MISC).size(0.25F, 0.25F).tracker(64, 3, true).name("item_revitalization_substance")
	
	val FALLING_BLOCK_HEAVY = build<EntityFallingBlockHeavy>(MISC).size(0.98F, 0.98F).tracker(160, 20, true).name("falling_block_heavy")
	val FALLING_OBSIDIAN    = build<EntityFallingObsidian>(MISC).size(0.98F, 0.98F).tracker(160, 20, true).name("falling_obsidian")
	val INFUSED_TNT         = build<EntityInfusedTNT>(MISC).size(0.98F, 0.98F).tracker(160, 10, true).name("infused_tnt")
	val TOKEN_HOLDER        = build<EntityTokenHolder>(MISC).size(0.55F, 0.675F).tracker(128, 60, false).name("token_holder")
	
	val ENDER_EYE = build<EntityBossEnderEye>(MONSTER).size(1.1F, 1F).immuneToFire().tracker(160, 1, true).name("ender_eye")
	
	val ENDERMAN              = build<EntityMobEnderman>(MONSTER).size(0.6F, 2.9F).tracker(80, 3, true).name("enderman")
	val ANGRY_ENDERMAN        = build<EntityMobAngryEnderman>(MONSTER).size(0.6F, 2.9F).tracker(80, 3, true).name("angry_enderman")
	val ENDERMAN_MUPPET       = build<EntityMobEndermanMuppet>(MISC).size(0.6F, 2.9F).tracker(96, 3, false).name("enderman_muppet")
	val ENDERMITE             = build<EntityMobEndermite>(MONSTER).size(0.425F, 0.325F).tracker(80, 3, true).name("endermite")
	val ENDERMITE_INSTABILITY = build<EntityMobEndermiteInstability>(MONSTER).size(0.425F, 0.325F).tracker(96, 3, true).name("endermite_instability")
	val SILVERFISH            = build<EntityMobSilverfish>(MONSTER).size(0.575F, 0.35F).tracker(80, 3, true).name("silverfish")
	val SPIDERLING            = build<EntityMobSpiderling>(MONSTER).size(0.675F, 0.45F).tracker(80, 2, true).name("spiderling")
	val UNDREAD               = build<EntityMobUndread>(MONSTER).size(0.625F, 1.925F).tracker(80, 3, true).name("undread")
	val VAMPIRE_BAT           = build<EntityMobVampireBat>(MONSTER).size(0.5F, 0.9F).tracker(80, 3, true).name("vampire_bat")
	val VILLAGER_DYING        = build<EntityMobVillagerDying>(MISC).size(0.6F, 1.95F).tracker(80, 3, false).name("villager_dying")
	
	val ENDER_PEARL  = build<EntityProjectileEnderPearl>(MISC).size(0.35F, 0.35F).tracker(64, 10, true).name("ender_pearl")
	val EYE_OF_ENDER = build<EntityProjectileEyeOfEnder>(MISC).size(0.5F, 1F).tracker(64, 60, false).name("eye_of_ender")
	val SPATIAL_DASH = build<EntityProjectileSpatialDash>(MISC).size(0.2F, 0.2F).tracker(64, 10, true).name("spatial_dash")
	
	val CAUSATUM_EVENT      = build<EntityTechnicalCausatumEvent>(MISC).size(0F, 0F).immuneToFire().tracker(1, Int.MAX_VALUE, false).name("causatum_event")
	val IGNEOUS_PLATE_LOGIC = build<EntityTechnicalIgneousPlateLogic>(MISC).size(0F, 0F).tracker(32, 10, false).name("igneous_plate_logic")
	val TECHNICAL_PUZZLE    = build<EntityTechnicalPuzzle>(MISC).size(0F, 0F).immuneToFire().tracker(1, Int.MAX_VALUE, false).name("technical_puzzle")
	val TECHNICAL_TRIGGER   = build<EntityTechnicalTrigger>(MISC).size(0F, 0F).immuneToFire().tracker(256, Int.MAX_VALUE, false).name("technical_trigger")
	
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<EntityType<*>>){
		with(e.registry){
			register(ITEM_CAULDRON_TRIGGER)
			register(ITEM_FRESHLY_COOKED)
			register(ITEM_IGNEOUS_ROCK)
			register(ITEM_NO_BOB)
			register(ITEM_REVITALIZATION_SUBSTANCE)
			
			register(FALLING_BLOCK_HEAVY)
			register(FALLING_OBSIDIAN)
			register(INFUSED_TNT)
			register(TOKEN_HOLDER)
			
			register(ENDER_EYE)
			
			register(ENDERMAN)
			register(ANGRY_ENDERMAN)
			register(ENDERMAN_MUPPET)
			register(ENDERMITE)
			register(ENDERMITE_INSTABILITY)
			register(SILVERFISH)
			register(SPIDERLING)
			register(UNDREAD)
			register(VAMPIRE_BAT)
			register(VILLAGER_DYING)
			
			register(ENDER_PEARL)
			register(EYE_OF_ENDER)
			register(SPATIAL_DASH)
			
			register(CAUSATUM_EVENT)
			register(IGNEOUS_PLATE_LOGIC)
			register(TECHNICAL_PUZZLE)
			register(TECHNICAL_TRIGGER)
		}
		
		// spawns
		
		val defaultSpawnPredicate = MonsterEntity::func_223325_c // RENAME this is the one that includes a light level check
		
		EntitySpawnPlacementRegistry.register(ENDERMAN, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, EntityMobEnderman.Companion::canSpawnAt)
		EntitySpawnPlacementRegistry.register(ANGRY_ENDERMAN, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicate)
		EntitySpawnPlacementRegistry.register(ENDERMITE, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicate)
		EntitySpawnPlacementRegistry.register(ENDERMITE_INSTABILITY, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicate)
		EntitySpawnPlacementRegistry.register(SILVERFISH, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicate)
		EntitySpawnPlacementRegistry.register(SPIDERLING, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicate)
		EntitySpawnPlacementRegistry.register(UNDREAD, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, defaultSpawnPredicate)
		EntitySpawnPlacementRegistry.register(VAMPIRE_BAT, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn)
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	// Vanilla modifications
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	fun onEntityJoinWorld(e: EntityJoinWorldEvent){ // UPDATE any better way?
		val world = e.world
		val original = e.entity
		
		val overriden = when(original.javaClass){
			EntityEnderman::class.java -> EntityMobEnderman(world)
			EntityEndermite::class.java -> EntityMobEndermite(world)
			EntitySilverfish::class.java -> EntityMobSilverfish(world)
			else -> return
		}
		
		// UPDATE check passengers
		overriden.read(original.writeWithoutTypeId(TagCompound()))
		
		e.isCanceled = true
		world.addEntity(overriden)
	}
	
	// Utilities
	
	private inline fun <reified T : Entity> build(classification: EntityClassification): EntityType.Builder<T>{
		return EntityType.Builder.create(EntityConstructors.get(T::class.java), classification)
	}
	
	private fun <T : Entity> EntityType.Builder<T>.tracker(trackingRange: Int, updateInterval: Int, receiveVelocityUpdates: Boolean): EntityType.Builder<T>{
		return this.setTrackingRange(trackingRange).setUpdateInterval(updateInterval).setShouldReceiveVelocityUpdates(receiveVelocityUpdates)
	}
	
	private fun <T : Entity> EntityType.Builder<T>.name(name: String): EntityType<T>{
		return this.build("hee.$name") named name
	}
}
