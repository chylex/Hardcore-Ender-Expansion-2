package chylex.hee.game.entity.living.controller

import chylex.hee.game.entity.living.path.PathNavigateGroundPreferBeeLine
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.toRadians
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.entity.ai.controller.MovementController
import net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO
import net.minecraft.entity.ai.controller.MovementController.Action.WAIT
import kotlin.math.cos

class EntityMoveJumping(entity: MobEntity, private val jumpDelay: () -> Int, degreeDiffBeforeMovement: Double) : MovementController(entity) {
	private val dotBeforeMovement = cos(degreeDiffBeforeMovement.toRadians())
	private var jumpDelayRemaining = 0
	
	override fun tick() {
		if (action == MOVE_TO) {
			action = WAIT
			mob.lookController.setLookPosition(posX, posY, posZ)
			
			if (mob.isOnGround) {
				disableMovement()
				
				if (jumpDelayRemaining == 0) {
					jumpDelayRemaining = jumpDelay()
					mob.jumpController.setJumping()
					enableMovement()
				}
			}
			else {
				enableMovement()
			}
		}
		else {
			disableMovement()
			
			if (action == WAIT && jumpDelayRemaining == 0 && mob.lookController.let { it.isLooking && !isLookingAt(it.lookPosX, it.lookPosZ) }) {
				mob.jumpController.setJumping()
			}
		}
		
		if (jumpDelayRemaining > 0) {
			--jumpDelayRemaining
		}
	}
	
	private fun isLookingAt(x: Double, z: Double): Boolean {
		val currentLook = Vec3.fromYaw(mob.rotationYawHead)
		val targetLook = Vec3.xz(x, z).subtract(mob.posX, 0.0, mob.posZ).normalize()
		
		return currentLook.dotProduct(targetLook) > dotBeforeMovement
	}
	
	private fun waitForRotation(): Boolean {
		val canWork = when (val navigator = mob.navigator) {
			is PathNavigateGroundPreferBeeLine -> navigator.isBeelining
			else                               -> navigator.noPath()
		}
		
		if (!canWork) {
			return false // the pathfinding is not granular enough to support rotations
		}
		
		return !isLookingAt(posX, posZ)
	}
	
	private fun enableMovement() {
		mob.aiMoveSpeed = if (waitForRotation()) 0F else (mob.getAttributeValue(MOVEMENT_SPEED) * speed).toFloat()
	}
	
	private fun disableMovement() {
		mob.aiMoveSpeed = 0F
	}
}
