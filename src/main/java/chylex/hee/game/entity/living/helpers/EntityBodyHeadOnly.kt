package chylex.hee.game.entity.living.helpers
import chylex.hee.system.migration.vanilla.EntityLiving
import net.minecraft.entity.ai.controller.BodyController

class EntityBodyHeadOnly(private val entity: EntityLiving) : BodyController(entity){
	override fun updateRenderAngles(){
		val yaw = entity.rotationYawHead
		
		entity.rotationYaw = yaw
		entity.renderYawOffset = yaw
	}
}
