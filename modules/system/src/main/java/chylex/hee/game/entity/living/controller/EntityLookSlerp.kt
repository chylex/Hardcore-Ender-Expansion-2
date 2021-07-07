package chylex.hee.game.entity.living.controller

import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.util.math.Quaternion
import chylex.hee.util.math.Vec
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.toPitch
import chylex.hee.util.math.toRadians
import chylex.hee.util.math.toYaw
import net.minecraft.entity.Entity
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.controller.LookController
import kotlin.math.cos

class EntityLookSlerp(entity: MobEntity, private var adjustmentSpeed: Float, maxInstantAngle: Float) : LookController(entity) {
	private var prevMaxInstantAngle = maxInstantAngle
	private var maxInstantAngleCos = cos(maxInstantAngle.toRadians())
	
	fun setRotationParams(adjustmentSpeed: Float, maxInstantAngle: Float) {
		this.adjustmentSpeed = adjustmentSpeed
		
		if (maxInstantAngle != prevMaxInstantAngle) {
			this.prevMaxInstantAngle = maxInstantAngle
			this.maxInstantAngleCos = cos(maxInstantAngle.toRadians())
		}
	}
	
	override fun setLookPosition(x: Double, y: Double, z: Double, deltaYaw: Float, deltaPitch: Float) {
		posX = x
		posY = y
		posZ = z
		isLooking = true
	}
	
	override fun setLookPositionWithEntity(entity: Entity, deltaYaw: Float, deltaPitch: Float) {
		entity.lookPosVec.let { setLookPosition(it.x, it.y, it.z, deltaYaw, deltaPitch) }
	}
	
	override fun tick() {
		if (isLooking) {
			isLooking = false
			
			val dir = mob.lookPosVec.directionTowards(Vec(posX, posY, posZ))
			
			if (Vec3.fromPitchYaw(mob.rotationPitch, mob.rotationYawHead).dotProduct(dir) >= maxInstantAngleCos) {
				mob.rotationYawHead = dir.toYaw()
				mob.rotationPitch = dir.toPitch()
			}
			else {
				val current = Quaternion.fromYawPitch(mob.rotationYawHead, mob.rotationPitch)
				val target = Quaternion.fromYawPitch(dir.toYaw(), dir.toPitch())
				
				val next = current.slerp(target, adjustmentSpeed)
				mob.rotationYawHead = next.rotationYaw
				mob.rotationPitch = next.rotationPitch
			}
		}
	}
}
