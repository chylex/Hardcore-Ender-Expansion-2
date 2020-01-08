package chylex.hee.game.entity.living.helpers
import chylex.hee.system.migration.vanilla.EntityLiving
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.math.Quaternion
import chylex.hee.system.util.toPitch
import chylex.hee.system.util.toRadians
import chylex.hee.system.util.toYaw
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.controller.LookController
import net.minecraft.util.math.Vec3d
import kotlin.math.cos

class EntityLookSlerp(entity: EntityLiving, private val adjustmentSpeed: Float, maxInstantAngle: Float) : LookController(entity){
	private val maxInstantAngleCos = cos(maxInstantAngle.toRadians())
	
	override fun setLookPosition(x: Double, y: Double, z: Double, deltaYaw: Float, deltaPitch: Float){
		posX = x
		posY = y
		posZ = z
		isLooking = true
	}
	
	override fun setLookPositionWithEntity(entity: Entity, deltaYaw: Float, deltaPitch: Float){
		entity.lastPortalVec.let { setLookPosition(it.x, it.y, it.z, deltaYaw, deltaPitch) }
	}
	
	override fun tick(){
		if (isLooking){
			isLooking = false
			
			val dir = mob.lookPosVec.directionTowards(Vec3d(posX, posY, posZ))
			
			if (Vec3d.fromPitchYaw(mob.rotationPitch, mob.rotationYaw).dotProduct(dir) >= maxInstantAngleCos){
				mob.rotationYaw = dir.toYaw()
				mob.rotationPitch = dir.toPitch()
			}
			else{
				val current = Quaternion.fromYawPitch(mob.rotationYaw, mob.rotationPitch)
				val target = Quaternion.fromYawPitch(dir.toYaw(), dir.toPitch())
				
				val next = current.slerp(target, adjustmentSpeed)
				mob.rotationYaw = next.rotationYaw
				mob.rotationPitch = next.rotationPitch
			}
		}
	}
}
