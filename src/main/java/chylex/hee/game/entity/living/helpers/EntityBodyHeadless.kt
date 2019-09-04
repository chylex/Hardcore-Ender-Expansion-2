package chylex.hee.game.entity.living.helpers
import net.minecraft.entity.EntityBodyHelper
import net.minecraft.entity.EntityLivingBase

class EntityBodyHeadless(private val entity: EntityLivingBase) : EntityBodyHelper(entity){
	override fun updateRenderAngles(){
		val yaw = entity.rotationYaw
		
		entity.renderYawOffset = yaw
		entity.rotationYawHead = yaw
	}
}
