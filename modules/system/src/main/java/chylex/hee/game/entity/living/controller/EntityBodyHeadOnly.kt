package chylex.hee.game.entity.living.controller

import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.controller.BodyController

class EntityBodyHeadOnly(private val entity: MobEntity) : BodyController(entity) {
	override fun updateRenderAngles() {
		val yaw = entity.rotationYawHead
		
		entity.rotationYaw = yaw
		entity.renderYawOffset = yaw
	}
}
