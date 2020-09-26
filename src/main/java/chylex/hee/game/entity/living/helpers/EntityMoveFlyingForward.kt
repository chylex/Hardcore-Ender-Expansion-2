package chylex.hee.game.entity.living.helpers
import chylex.hee.system.migration.vanilla.EntityLiving
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.square
import net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED
import net.minecraft.entity.ai.controller.MovementController
import net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO
import net.minecraft.entity.ai.controller.MovementController.Action.WAIT
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt

class EntityMoveFlyingForward(entity: EntityLiving) : MovementController(entity){
	companion object{
		const val AIR_FRICTION = 0.216F // from EntityLivingBase.travel
	}
	
	override fun tick(){
		if (action == MOVE_TO){
			action = WAIT
			
			val diff = mob.lookPosVec.directionTowards(Vec3d(posX, posY, posZ))
			val xz = sqrt(square(diff.x) + square(diff.z))
			
			val dot = Vec3d.fromPitchYaw(mob.rotationPitch, mob.rotationYaw).dotProduct(diff).coerceAtLeast(0.0)
			val speed = mob.getAttribute(FLYING_SPEED).value * square(dot) * super.speed
			
			mob.setMoveForward((xz * speed).toFloat())
			mob.setMoveVertical((diff.y * speed).toFloat())
		}
		else{
			mob.setMoveForward(0F)
			mob.setMoveVertical(0F)
		}
	}
}
