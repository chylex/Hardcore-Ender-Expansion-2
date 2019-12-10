package chylex.hee.game.entity.living.helpers
import chylex.hee.system.util.getAttribute
import chylex.hee.system.util.toDegrees
import chylex.hee.system.util.value
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED
import net.minecraft.entity.ai.EntityMoveHelper
import net.minecraft.entity.ai.EntityMoveHelper.Action.MOVE_TO
import net.minecraft.entity.ai.EntityMoveHelper.Action.WAIT
import net.minecraft.util.math.MathHelper
import kotlin.math.atan2
import kotlin.math.sign

class EntityMoveFlyingBat(entity: EntityLiving) : EntityMoveHelper(entity){
	override fun onUpdateMoveHelper(){
		if (action != MOVE_TO){
			return
		}
		
		action = WAIT
		
		val finalSpeed = entity.getAttribute(FLYING_SPEED).value * speed
		
		entity.motionX += ((sign(posX - entity.posX) * 0.5) - entity.motionX) * finalSpeed
		entity.motionY += ((sign(posY - entity.posY) * 0.7) - entity.motionY) * finalSpeed
		entity.motionZ += ((sign(posZ - entity.posZ) * 0.5) - entity.motionZ) * finalSpeed
		
		entity.moveForward = (5F * finalSpeed).toFloat()
		entity.rotationYaw += MathHelper.wrapDegrees(atan2(entity.motionZ, entity.motionX).toDegrees() - 90F - entity.rotationYaw).toFloat()
	}
}
