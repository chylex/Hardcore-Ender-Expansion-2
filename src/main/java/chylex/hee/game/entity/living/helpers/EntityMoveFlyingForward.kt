package chylex.hee.game.entity.living.helpers
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.square
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.ai.EntityMoveHelper
import net.minecraft.entity.ai.EntityMoveHelper.Action.MOVE_TO
import net.minecraft.entity.ai.EntityMoveHelper.Action.WAIT
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt

class EntityMoveFlyingForward(entity: EntityLiving) : EntityMoveHelper(entity){
	override fun onUpdateMoveHelper(){
		if (action == MOVE_TO){
			action = WAIT
			
			val diff = Vec3d(posX, posY, posZ).subtract(entity.lookPosVec).normalize()
			val xz = sqrt(square(diff.x) + square(diff.z))
			
			val dot = Vec3d.fromPitchYaw(entity.rotationPitch, entity.rotationYaw).dotProduct(diff).coerceAtLeast(0.0)
			val speed = entity.getEntityAttribute(MOVEMENT_SPEED).attributeValue * square(dot)
			
			entity.setMoveForward((xz * speed).toFloat())
			entity.setMoveVertical((diff.y * speed).toFloat())
		}
		else{
			entity.setMoveForward(0F)
			entity.setMoveVertical(0F)
		}
	}
}
