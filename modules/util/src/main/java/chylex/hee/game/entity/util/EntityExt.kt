package chylex.hee.game.entity.util

import chylex.hee.util.math.Vec
import net.minecraft.enchantment.ProtectionEnchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.util.math.vector.Vector3d

// Properties

var Entity.posVec: Vector3d
	get() = this.positionVec
	set(value) = this.setRawPosition(value.x, value.y, value.z)

fun Entity.setX(x: Double) {
	this.setRawPosition(x, posY, posZ)
}

fun Entity.setY(y: Double) {
	this.setRawPosition(posX, y, posZ)
}

fun Entity.setZ(z: Double) {
	this.setRawPosition(posX, posY, z)
}

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

val Entity.lookPosVec: Vector3d
	get() = this.getEyePosition(1F)

val Entity.lookDirVec: Vector3d
	get() = this.getLook(1F)

fun Entity.setFireTicks(ticks: Int) {
	val prevFireTicks = this.fireTimer
	this.setFire(ticks / 20) // in case something overrides it
	
	val finalTicks = when (this) {
		is LivingEntity -> ProtectionEnchantment.getFireTimeForEntity(this, ticks)
		else            -> ticks
	}
	
	if (finalTicks > prevFireTicks) {
		this.forceFireTicks(finalTicks)
	}
}

fun ItemEntity.cloneFrom(source: Entity) {
	copyLocationAndAnglesFrom(source)
	motion = source.motion
	isAirBorne = source.isAirBorne
	
	if (source is ItemEntity) {
		lifespan = source.lifespan
		pickupDelay = source.pickupDelay
		
		throwerId = source.throwerId
		ownerId = source.ownerId
	}
}

fun Entity.isAnyVulnerablePlayerWithinRange(range: Double): Boolean {
	return world.getClosestPlayer(posX, posY, posZ, range, true) != null
}
