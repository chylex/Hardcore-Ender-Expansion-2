package chylex.hee.game.entity.living.controller

import chylex.hee.game.entity.util.motionX
import chylex.hee.game.entity.util.motionZ
import chylex.hee.util.math.toDegrees
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.attributes.Attributes.FLYING_SPEED
import net.minecraft.entity.ai.controller.MovementController
import net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO
import net.minecraft.entity.ai.controller.MovementController.Action.WAIT
import net.minecraft.util.math.MathHelper
import kotlin.math.atan2
import kotlin.math.sign

class EntityMoveFlyingBat(entity: MobEntity) : MovementController(entity) {
	override fun tick() {
		if (action != MOVE_TO) {
			return
		}
		
		action = WAIT
		
		val finalSpeed = mob.getAttributeValue(FLYING_SPEED) * speed
		
		mob.motion = mob.motion.let {
			it.add(
				((sign(posX - mob.posX) * 0.5) - it.x) * finalSpeed,
				((sign(posY - mob.posY) * 0.7) - it.y) * finalSpeed,
				((sign(posZ - mob.posZ) * 0.5) - it.z) * finalSpeed
			)
		}
		
		mob.moveForward = (5F * finalSpeed).toFloat()
		mob.rotationYaw += MathHelper.wrapDegrees(atan2(mob.motionZ, mob.motionX).toDegrees() - 90F - mob.rotationYaw).toFloat()
	}
}
