package chylex.hee.system.util
import chylex.hee.game.entity.living.ai.AITargetRandom
import chylex.hee.game.entity.living.ai.AITargetSwarmSwitch
import chylex.hee.game.entity.util.EntitySelector
import com.google.common.base.Predicate
import com.google.common.base.Predicates
import net.minecraft.enchantment.EnchantmentProtection
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.ai.EntityAIAttackMelee
import net.minecraft.entity.ai.EntityAIBase
import net.minecraft.entity.ai.EntityAIHurtByTarget
import net.minecraft.entity.ai.EntityAILookIdle
import net.minecraft.entity.ai.EntityAINearestAttackableTarget
import net.minecraft.entity.ai.EntityAISwimming
import net.minecraft.entity.ai.EntityAIWander
import net.minecraft.entity.ai.EntityAIWanderAvoidWater
import net.minecraft.entity.ai.EntityAIWatchClosest
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.EntitySelectors
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

// Properties

var Entity.posVec: Vec3d
	get() = this.positionVector
	set(value){
		this.posX = value.x
		this.posY = value.y
		this.posZ = value.z
	}

var Entity.motionVec: Vec3d
	get() = Vec3d(this.motionX, this.motionY, this.motionZ)
	set(value){
		this.motionX = value.x
		this.motionY = value.y
		this.motionZ = value.z
	}

val Entity.lookPosVec: Vec3d
	get() = this.getPositionEyes(1F)

val Entity.lookDirVec: Vec3d
	get() = this.getLook(1F)

// Methods

fun Entity.setFireTicks(ticks: Int){
	val prevFire = this.fire
	this.setFire(ticks / 20) // in case something overrides it
	
	val finalTicks = when{
		this is EntityLivingBase -> EnchantmentProtection.getFireTimeForEntity(this, ticks)
		else -> ticks
	}
	
	if (finalTicks > prevFire){
		this.fire = finalTicks
	}
}

fun EntityItem.cloneFrom(source: Entity){
	motionX = source.motionX
	motionY = source.motionY
	motionZ = source.motionZ
	isAirBorne = source.isAirBorne
	
	if (source is EntityItem){
		lifespan = source.lifespan
		pickupDelay = source.pickupDelay
		
		thrower = source.thrower
		owner = source.owner
	}
}

// AI

typealias AIBase = EntityAIBase

/** Makes the AI compatible with everything. */
const val AI_FLAG_NONE = 0

/** Prevents other movement AI tasks from running (mutex 1, 3, 5, 7). */
const val AI_FLAG_MOVEMENT = 0b001

/** Prevents other looking AI tasks from running (mutex 2, 3, 6, 7). */
const val AI_FLAG_LOOKING = 0b010

/** Prevents other swimming AI tasks from running (mutex 4, 5, 6, 7). */
const val AI_FLAG_SWIMMING = 0b100

// AI (Movement)

inline fun AISwim(entity: EntityCreature) =
	EntityAISwimming(entity)

inline fun AIWander(entity: EntityCreature, movementSpeed: Double, chancePerTick: Int) =
	EntityAIWander(entity, movementSpeed, chancePerTick)

inline fun AIWanderNoWater(entity: EntityCreature, movementSpeed: Double, chancePerTick: Int) =
	object : EntityAIWanderAvoidWater(entity, movementSpeed, 0F){
		init{
			executionChance = chancePerTick
		}
	}

// AI (Looking)

inline fun AIWatchIdle(entity: EntityCreature) =
	EntityAILookIdle(entity)

inline fun <reified T : EntityLivingBase> AIWatchClosest(entity: EntityCreature, maxDistance: Float) =
	EntityAIWatchClosest(entity, T::class.java, maxDistance)

// AI (Actions)

inline fun AIAttackMelee(entity: EntityCreature, movementSpeed: Double, chaseAfterLosingSight: Boolean) =
	EntityAIAttackMelee(entity, movementSpeed, chaseAfterLosingSight)

// AI (Targeting)

inline fun AITargetAttacker(entity: EntityCreature, callReinforcements: Boolean) =
	EntityAIHurtByTarget(entity, callReinforcements)

inline fun <reified T : EntityLivingBase> AITargetNearby(entity: EntityCreature, checkSight: Boolean, easilyReachableOnly: Boolean, chancePerTick: Int, targetPredicate: Predicate<T>? = null) =
	EntityAINearestAttackableTarget<T>(entity, T::class.java, chancePerTick, checkSight, easilyReachableOnly, targetPredicate)

inline fun <reified T : EntityLivingBase> AITargetRandom(entity: EntityCreature, checkSight: Boolean, easilyReachableOnly: Boolean, chancePerTick: Int, noinline targetPredicate: ((T) -> Boolean)? = null) =
	AITargetRandom(entity, checkSight, easilyReachableOnly, chancePerTick, T::class.java, targetPredicate)

inline fun <reified T : EntityLivingBase> AITargetSwarmSwitch(entity: EntityCreature, checkSight: Boolean, easilyReachableOnly: Boolean, rangeMultiplier: Float, noinline targetPredicate: ((T) -> Boolean)? = null) =
	AITargetSwarmSwitch(entity, checkSight, easilyReachableOnly, rangeMultiplier, T::class.java, targetPredicate)

// Selectors

private val predicateAliveAndNotSpectating = Predicates.and(EntitySelectors.IS_ALIVE, EntitySelectors.NOT_SPECTATING)
private val predicateAliveAndTargetable = Predicates.and(EntitySelectors.IS_ALIVE, EntitySelectors.CAN_AI_TARGET) // UPDATE: Make sure CAN_AI_TARGET still only checks creative/spectator mode
private val predicateAlwaysTrue = Predicates.alwaysTrue<Entity>()

/**
 * Selects all entities which are not spectators.
 */
val World.selectEntities
	get() = EntitySelector(this, EntitySelectors.NOT_SPECTATING)

/**
 * Selects all entities which have not been removed from the world, and are not spectators.
 */
val World.selectExistingEntities
	get() = EntitySelector(this, predicateAliveAndNotSpectating)

/**
 * Selects all entities which have not been removed from the world, and are not spectators or creative mode players.
 */
val World.selectVulnerableEntities
	get() = EntitySelector(this, predicateAliveAndTargetable)

/**
 * Selects all entities and spectators.
 */
val World.selectEntitiesAndSpectators
	get() = EntitySelector(this, predicateAlwaysTrue)
