package chylex.hee.game.entity.living

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.IHeeMobEntityType
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.entity.living.ai.AttackMelee
import chylex.hee.game.entity.living.ai.ForceWanderTiming
import chylex.hee.game.entity.living.ai.Swim
import chylex.hee.game.entity.living.ai.TargetAttacker
import chylex.hee.game.entity.living.ai.TargetNearby
import chylex.hee.game.entity.living.ai.WanderLand
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntitySpawnPlacement
import chylex.hee.game.entity.util.DefaultEntityAttributes
import chylex.hee.game.entity.util.with
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.math.square
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.entity.monster.EndermiteEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.IPacket
import net.minecraft.util.ResourceLocation
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

open class EntityMobEndermite(type: EntityType<out EntityMobEndermite>, world: World) : EndermiteEntity(type, world) {
	@Suppress("unused")
	constructor(world: World) : this(ModEntities.ENDERMITE, world)
	
	open class BaseType<T : EntityMobEndermite> : IHeeMobEntityType<T> {
		final override val classification
			get() = EntityClassification.MONSTER
		
		final override val size
			get() = EntitySize(0.425F, 0.325F)
		
		override val attributes
			get() = DefaultEntityAttributes.endermite.with(
				MAX_HEALTH    to 8.0,
				ATTACK_DAMAGE to 2.0,
			)
		
		final override val placement
			get() = EntitySpawnPlacement.hostile<T>()
	}
	
	object Type : BaseType<EntityMobEndermite>() {
		override val localization
			get() = LocalizationStrategy.None
	}
	
	companion object {
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS)
		
		private const val AGE_TAG = "Age"
		private const val IDLE_TAG = "Idle"
	}
	
	private var realLifetime = 0
	private var idleDespawnTimer: Short = 0
	
	init {
		experienceValue = 3
	}
	
	override fun registerGoals() {
		val aiWander = WanderLand(this, movementSpeed = 1.0, chancePerTick = 50)
		
		goalSelector.addGoal(1, Swim(this))
		goalSelector.addGoal(2, AttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false))
		goalSelector.addGoal(3, aiWander)
		goalSelector.addGoal(4, ForceWanderTiming(this, aiWander, defaultChancePerTick = 50, forcedTimingRange = 10..74))
		// no watching AI because it makes Endermites spazz out
		
		targetSelector.addGoal(1, TargetAttacker(this, callReinforcements = true))
		targetSelector.addGoal(2, TargetNearby<PlayerEntity>(this, chancePerTick = 10, checkSight = true, easilyReachableOnly = false))
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun livingTick() {
		idleTime = 0
		lifetime = 0
		++realLifetime
		super.livingTick()
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean {
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	override fun getLootTable(): ResourceLocation {
		return Resource.Custom("entities/endermite_natural")
	}
	
	override fun getCreatureAttribute(): CreatureAttribute {
		return CustomCreatureType.ENDER
	}
	
	override fun checkDespawn() {
		if (world.difficulty == PEACEFUL && isDespawnPeaceful) {
			remove()
			return
		}
		
		if (isNoDespawnRequired || preventDespawn()) {
			return
		}
		
		val closest = world.getClosestPlayer(this, -1.0)
		
		if (closest == null) {
			return
		}
		
		val distance = closest.getDistanceSq(this)
		
		if (distance > square(128)) {
			remove()
		}
		else if (distance > square(32)) {
			if (++idleDespawnTimer >= 900 || (realLifetime % 20 == 0 && rng.nextInt(50) == 0)) {
				remove()
			}
		}
		else {
			idleDespawnTimer = 0
		}
	}
	
	override fun canDespawn(distanceToClosestPlayerSq: Double): Boolean {
		return realLifetime > 1800
	}
	
	override fun preventDespawn(): Boolean {
		return super.preventDespawn() || realLifetime <= 1800
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putInt(AGE_TAG, realLifetime)
		putShort(IDLE_TAG, idleDespawnTimer)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		realLifetime = getInt(AGE_TAG)
		idleDespawnTimer = getShort(IDLE_TAG)
	}
}
