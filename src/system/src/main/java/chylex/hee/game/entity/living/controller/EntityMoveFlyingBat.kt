package chylex.hee.game.entity.living.controller

import chylex.hee.game.entity.motionX
import chylex.hee.game.entity.motionZ
import chylex.hee.system.math.toDegrees
import chylex.hee.system.migration.EntityLiving
import net.minecraft.entity.ai.attributes.Attributes.FLYING_SPEED
import net.minecraft.entity.ai.controller.MovementController
import net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO
import net.minecraft.entity.ai.controller.MovementController.Action.WAIT
import net.minecraft.util.math.MathHelper
import kotlin.math.atan2
import kotlin.math.sign

class EntityMoveFlyingBat(entity: EntityLiving) : MovementController(entity) {
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
