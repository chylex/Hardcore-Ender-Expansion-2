package chylex.hee.game.entity.living.helpers
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.math.Quaternion
import chylex.hee.system.util.toPitch
import chylex.hee.system.util.toRadians
import chylex.hee.system.util.toYaw
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.ai.EntityLookHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.cos

class EntityLookSlerp(private val entity: EntityLiving, private val adjustmentSpeed: Float, maxInstantAngle: Float) : EntityLookHelper(entity){
	private val maxInstantAngleCos = cos(maxInstantAngle.toRadians())
	
	private var target = Vec3d.ZERO
	private var isLooking = false
	
	override fun setLookPosition(x: Double, y: Double, z: Double, deltaYaw: Float, deltaPitch: Float){
		target = Vec3d(x, y, z)
		isLooking = true
	}
	
	override fun setLookPositionWithEntity(entity: Entity, deltaYaw: Float, deltaPitch: Float){
		target = entity.lookPosVec
		isLooking = true
	}
	
	override fun onUpdateLook(){
		if (isLooking){
			isLooking = false
			
			val dir = entity.lookPosVec.directionTowards(target)
			
			if (Vec3d.fromPitchYaw(entity.rotationPitch, entity.rotationYaw).dotProduct(dir) >= maxInstantAngleCos){
				entity.rotationYaw = dir.toYaw()
				entity.rotationPitch = dir.toPitch()
			}
			else{
				val current = Quaternion.fromYawPitch(entity.rotationYaw, entity.rotationPitch)
				val target = Quaternion.fromYawPitch(dir.toYaw(), dir.toPitch())
				
				val next = current.slerp(target, adjustmentSpeed)
				entity.rotationYaw = next.rotationYaw
				entity.rotationPitch = next.rotationPitch
			}
		}
	}
	
	override fun getIsLooking() = isLooking
	override fun getLookPosX() = target.x
	override fun getLookPosY() = target.y
	override fun getLookPosZ() = target.z
}
