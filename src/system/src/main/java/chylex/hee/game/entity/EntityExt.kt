package chylex.hee.game.entity

import chylex.hee.system.math.Vec
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.serialization.getCompoundOrNull
import chylex.hee.system.serialization.getOrCreateCompound
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.heeTagOrNull
import com.google.common.base.Predicates
import com.google.common.collect.Iterables
import net.minecraft.client.world.ClientWorld
import net.minecraft.enchantment.ProtectionEnchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation
import net.minecraft.entity.ai.attributes.IAttributeInstance
import net.minecraft.entity.player.PlayerEntity.PERSISTED_NBT_TAG
import net.minecraft.util.EntityPredicates
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IEntityReader
import net.minecraft.world.IWorld
import net.minecraft.world.server.ServerWorld

// Properties

var Entity.posVec: Vec3d
	get() = this.positionVector
	set(value) = this.setRawPosition(value.x, value.y, value.z)

var Entity.positionX
	get() = this.posX
	set(value) = this.setRawPosition(value, posY, posZ)

var Entity.positionY
	get() = this.posY
	set(value) = this.setRawPosition(posX, value, posZ)

var Entity.positionZ
	get() = this.posZ
	set(value) = this.setRawPosition(posX, posY, value)

var Entity.motionX
	get() = this.motion.x
	set(value) {
		this.motion = Vec(value, motion.y, motion.z)
	}

var Entity.motionY
	get() = this.motion.y
	set(value) {
		this.motion = Vec(motion.x, value, motion.z)
	}

var Entity.motionZ
	get() = this.motion.z
	set(value) {
		this.motion = Vec(motion.x, motion.y, value)
	}

val Entity.lookPosVec: Vec3d
	get() = this.getEyePosition(1F)

val Entity.lookDirVec: Vec3d
	get() = this.getLook(1F)

// Methods

fun Entity.setFireTicks(ticks: Int) {
	val prevFire = this.fire
	this.setFire(ticks / 20) // in case something overrides it
	
	val finalTicks = when(this) {
		is EntityLivingBase -> ProtectionEnchantment.getFireTimeForEntity(this, ticks)
		else                -> ticks
	}
	
	if (finalTicks > prevFire) {
		this.fire = finalTicks
	}
}

fun EntityItem.cloneFrom(source: Entity) {
	setPositionAndRotation(source.posX, source.posY, source.posZ, source.rotationYaw, source.rotationPitch)
	motion = source.motion
	isAirBorne = source.isAirBorne
	
	if (source is EntityItem) {
		lifespan = source.lifespan
		pickupDelay = source.pickupDelay
		
		throwerId = source.throwerId
		ownerId = source.ownerId
	}
}

// NBT

val Entity.heeTag
	get() = this.persistentData.heeTag

val Entity.heeTagOrNull
	get() = this.persistentData.heeTagOrNull

val Entity.heeTagPersistent
	get() = this.persistentData.getOrCreateCompound(PERSISTED_NBT_TAG).heeTag

val Entity.heeTagPersistentOrNull
	get() = this.persistentData.getCompoundOrNull(PERSISTED_NBT_TAG)?.heeTagOrNull

// Attributes

/** Performs operation: base + x + y */
val OPERATION_ADD = Operation.ADDITION

/** Performs operation: base * (1 + x + y) */
val OPERATION_MUL_INCR_GROUPED = Operation.MULTIPLY_BASE

/** Performs operation: base * (1 + x) * (1 + y) */
val OPERATION_MUL_INCR_INDIVIDUAL = Operation.MULTIPLY_TOTAL

// Attributes (Helpers)

fun IAttributeInstance.tryApplyModifier(modifier: AttributeModifier) {
	if (!this.hasModifier(modifier)) {
		this.applyModifier(modifier)
	}
}

fun IAttributeInstance.tryRemoveModifier(modifier: AttributeModifier) {
	if (this.hasModifier(modifier)) {
		this.removeModifier(modifier)
	}
}

// Selectors

fun Entity.isAnyVulnerablePlayerWithinRange(range: Double): Boolean {
	return world.getClosestPlayer(posX, posY, posZ, range, true) != null
}

private val predicateAliveAndNotSpectating = EntityPredicates.IS_ALIVE.and(EntityPredicates.NOT_SPECTATING)
private val predicateAliveAndTargetable = EntityPredicates.IS_ALIVE.and(EntityPredicates.CAN_AI_TARGET)
private val predicateAlwaysTrue = Predicates.alwaysTrue<Entity>()

/**
 * Selects all entities in the dimension.
 */
val IWorld.selectAllEntities: Iterable<Entity>
	get() = when(this) {
		is ClientWorld -> this.allEntities
		is ServerWorld -> Iterables.concat(Iterable(this.entities::iterator), this.globalEntities)
		else           -> emptyList()
	}

/**
 * Selects all entities which are not spectators.
 */
val IEntityReader.selectEntities
	get() = EntitySelector(this, EntityPredicates.NOT_SPECTATING)

/**
 * Selects all entities which have not been removed from the world, and are not spectators.
 */
val IEntityReader.selectExistingEntities
	get() = EntitySelector(this, predicateAliveAndNotSpectating)

/**
 * Selects all entities which have not been removed from the world, and are not spectators or creative mode players.
 */
val IEntityReader.selectVulnerableEntities
	get() = EntitySelector(this, predicateAliveAndTargetable)

/**
 * Selects all entities and spectators.
 */
val IEntityReader.selectEntitiesAndSpectators
	get() = EntitySelector(this, predicateAlwaysTrue)
