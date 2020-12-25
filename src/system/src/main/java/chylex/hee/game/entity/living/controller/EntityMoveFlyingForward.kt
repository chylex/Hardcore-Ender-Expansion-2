package chylex.hee.game.entity.living.controller

import chylex.hee.game.entity.lookDirVec
import chylex.hee.game.entity.lookPosVec
import chylex.hee.system.math.Vec
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.sign
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityLiving
import net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED
import net.minecraft.entity.ai.controller.MovementController
import net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO
import net.minecraft.entity.ai.controller.MovementController.Action.STRAFE
import net.minecraft.entity.ai.controller.MovementController.Action.WAIT
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt

class EntityMoveFlyingForward(entity: EntityLiving, private val strafeSpeedMp: Double = 0.75) : MovementController(entity) {
	companion object {
		const val AIR_FRICTION = 0.216F // from EntityLivingBase.travel
	}
	
	fun strafe() {
		action = STRAFE
	}
	
	override fun strafe(moveForward: Float, moveStrafe: Float) {
		action = STRAFE
	}
	
	override fun tick() {
		if (action == STRAFE) {
			action = WAIT
			
			val dir = mob.lookDirVec
			val diff = mob.lookPosVec.directionTowards(Vec(posX, posY, posZ))
			
			val dot = dir.dotProduct(diff)
			val speed = mob.getAttribute(FLYING_SPEED).value * strafeSpeedMp * super.speed
			
			mob.setMoveVertical((diff.y * speed).toFloat())
			mob.setMoveForward((dot * speed).toFloat())
			mob.setMoveStrafing(((1F - dot) * dir.sign(diff) * speed).toFloat())
		}
		else if (action == MOVE_TO) {
			action = WAIT
			
			val diff = mob.lookPosVec.directionTowards(Vec(posX, posY, posZ))
			val xz = sqrt(square(diff.x) + square(diff.z))
			
			val dot = Vec3d.fromPitchYaw(mob.rotationPitch, mob.rotationYaw).dotProduct(diff).coerceAtLeast(0.0)
			val speed = mob.getAttribute(FLYING_SPEED).value * square(dot) * super.speed
			
			mob.setMoveVertical((diff.y * speed).toFloat())
			mob.setMoveForward((xz * speed).toFloat())
			mob.setMoveStrafing(0F)
		}
		else {
			mob.setMoveVertical(0F)
			mob.setMoveForward(0F)
			mob.setMoveStrafing(0F)
		}
	}
}
