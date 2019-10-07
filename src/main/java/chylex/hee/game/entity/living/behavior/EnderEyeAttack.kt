package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.square
import chylex.hee.system.util.totalTime

sealed class EnderEyeAttack{
	abstract val canTakeKnockback: Boolean
	abstract fun tick(entity: EntityBossEnderEye): Boolean
	
	class Melee : EnderEyeAttack(){
		override val canTakeKnockback = true
		private var lastAttackTime = 0L
		
		override fun tick(entity: EntityBossEnderEye): Boolean = with(entity){
			val target = attackTarget ?: forceFindNewTarget()
			
			if (target == null){
				armPosition = EntityBossEnderEye.ARMS_LIMP
				aiMoveSpeed = 0F
				setRotateTarget(null)
			}
			else{
				armPosition = EntityBossEnderEye.ARMS_HUG
				lookHelper.setLookPositionWithEntity(target, 0F, 0F)
				setRotateTarget(target)
				
				val currentVec = lookPosVec
				val targetVec = target.lookPosVec
				val distSq = targetVec.squareDistanceTo(currentVec)
				
				if (distSq > square(1.2)){
					moveHelper.setMoveTo(targetVec.x, targetVec.y, targetVec.z, 1.0)
				}
				else{
					val motVec = motionVec
					
					if (currentVec.directionTowards(targetVec).dotProduct(motVec.normalize()) > 0.0){
						motionVec = motVec.scale(0.4)
					}
				}
				
				if (distSq < square(1.4)){
					val currentTime = world.totalTime
					
					if (currentTime - lastAttackTime >= 20L){
						lastAttackTime = currentTime
						attackEntityAsMob(target)
					}
				}
			}
			
			return true
		}
	}
}
