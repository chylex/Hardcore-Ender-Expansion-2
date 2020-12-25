package chylex.hee.game.entity.living.controller

import chylex.hee.game.entity.living.path.PathNavigateGroundPreferBeeLine
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.toRadians
import chylex.hee.system.migration.EntityLiving
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.ai.controller.MovementController
import net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO
import net.minecraft.entity.ai.controller.MovementController.Action.WAIT
import kotlin.math.cos

class EntityMoveJumping(entity: EntityLiving, private val jumpDelay: () -> Int, degreeDiffBeforeMovement: Double) : MovementController(entity) {
	private val dotBeforeMovement = cos(degreeDiffBeforeMovement.toRadians())
	private var jumpDelayRemaining = 0
	
	override fun tick() {
		if (action == MOVE_TO) {
			action = WAIT
			mob.lookController.setLookPosition(posX, posY, posZ)
			
			if (mob.onGround) {
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
		val canWork = when(val navigator = mob.navigator) {
			is PathNavigateGroundPreferBeeLine -> navigator.isBeelining
			else                               -> navigator.noPath()
		}
		
		if (!canWork) {
			return false // the pathfinding is not granular enough to support rotations
		}
		
		return !isLookingAt(posX, posZ)
	}
	
	private fun enableMovement() {
		mob.aiMoveSpeed = if (waitForRotation()) 0F else (mob.getAttribute(MOVEMENT_SPEED).value * speed).toFloat()
	}
	
	private fun disableMovement() {
		mob.aiMoveSpeed = 0F
	}
}
