package chylex.hee.game.entity.living.helpers
import chylex.hee.system.migration.vanilla.EntityLiving
import net.minecraft.entity.ai.controller.BodyController

class EntityBodyHeadless(private val entity: EntityLiving) : BodyController(entity){
	override fun updateRenderAngles(){
		val yaw = entity.rotationYaw
		
		entity.renderYawOffset = yaw
		entity.rotationYawHead = yaw
	}
}
