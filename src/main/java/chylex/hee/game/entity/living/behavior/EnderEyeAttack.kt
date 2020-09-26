package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.square
import chylex.hee.system.util.totalTime

sealed class EnderEyeAttack{
	abstract val damageType: Damage
	abstract val damageMultiplier: Float
	abstract val canTakeKnockback: Boolean
	abstract fun tick(entity: EntityBossEnderEye): Boolean
	
	class Melee : EnderEyeAttack(){
		override val damageType
			get() = EntityBossEnderEye.DAMAGE_MELEE
		
		override val damageMultiplier
			get() = 1F
		
		override val canTakeKnockback = true
		private var movementSpeed = 1.0
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
				lookController.setLookPositionWithEntity(target, 0F, 0F)
				setRotateTarget(target)
				
				val currentVec = lookPosVec
				val targetVec = target.lookPosVec
				val distSq = targetVec.squareDistanceTo(currentVec)
				
				if (distSq > square(6.0 - ((movementSpeed - 1.0) / 1.5))){
					if (movementSpeed < 3.5){
						movementSpeed = (movementSpeed + 0.1).coerceAtMost(3.5)
					}
					
					moveHelper.setMoveTo(targetVec.x, targetVec.y + (movementSpeed - 1.0) * 0.6, targetVec.z, movementSpeed)
				}
				else if (distSq > square(1.2)){
					if (movementSpeed > 1.0){
						movementSpeed = (movementSpeed - 0.3).coerceAtLeast(1.0)
					}
					
					moveHelper.setMoveTo(targetVec.x, targetVec.y, targetVec.z, movementSpeed)
				}
				else{
					if (currentVec.directionTowards(targetVec).dotProduct(motion.normalize()) > 0.0){
						motion = motion.scale(0.4)
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
		
		fun reset(entity: EntityBossEnderEye){
			movementSpeed = 1.0
			lastAttackTime = entity.world.totalTime
		}
	}
}
