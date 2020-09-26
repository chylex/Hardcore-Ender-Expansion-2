package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.addY
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.lookDirVec
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.motionX
import chylex.hee.system.util.motionZ
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.toRadians
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.withY
import net.minecraft.util.math.AxisAlignedBB
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos

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
	
	class KnockbackDash : EnderEyeAttack(){
		override val damageType
			get() = EntityBossEnderEye.DAMAGE_DASH
		
		override val damageMultiplier
			get() = 2F
		
		override val canTakeKnockback = false
		
		private var isSlowingDown = true
		private var attackTimer = 0
		private var attackRepeats = 0
		private val hitEntities = mutableSetOf<UUID>()
		
		override fun tick(entity: EntityBossEnderEye): Boolean = with(entity){
			val target = attackTarget ?: return false
			
			if (isSlowingDown){
				armPosition = EntityBossEnderEye.ARMS_ATTACK
				aiMoveSpeed = 0F
				setRotateTarget(target)
				
				if (motion.withY(0.0).lengthSquared() < square(0.05)){
					if (attackRepeats == 0){
						isSlowingDown = false
					}
					else{
						val lookDir = lookDirVec
						val targetDir = target.lookPosVec.subtract(lookPosVec).normalize()
						
						if (abs(lookDir.dotProduct(targetDir)) > cos(10.0.toRadians())){
							isSlowingDown = false
						}
					}
				}
			}
			else{
				armPosition = EntityBossEnderEye.ARMS_HUG
				setRotateTarget(null)
				
				if (attackTimer == 0){
					motion = target.lookPosVec.subtract(lookPosVec).scale(rng.nextFloat(0.15, 0.18))
					attackTimer = 1
				}
				
				if (hitEntities.isNotEmpty()){
					++attackTimer
				}
				
				if (attackTimer == 24 || motion.withY(0.0).lengthSquared() < square(0.15)){
					if (health < realMaxHealth * 0.5F && attackRepeats == 0){
						++attackRepeats
						isSlowingDown = true
						attackTimer = 0
						hitEntities.clear()
						return true
					}
					else{
						return false
					}
				}
				
				causeDamageInFront(this)
			}
			
			return true
		}
		
		private fun causeDamageInFront(entity: EntityBossEnderEye) = with(entity){
			val frontHurtCenter = lookPosVec.add(lookDirVec.scale(width * 0.75))
			val frontHurtDist = width * 0.6
			
			for(hitEntity in world.selectVulnerableEntities.inBox<EntityLivingBase>(AxisAlignedBB(frontHurtCenter, frontHurtCenter).grow(frontHurtDist))){
				if (hitEntity.isNonBoss && !hitEntities.contains(hitEntity.uniqueID) && attackEntityAsMob(hitEntity)){
					val multiplier = when{
						hitEntity.isActiveItemStackBlocking -> 0.25
						hitEntity.isSneaking -> 0.75
						else -> 1.0
					}
					
					val knockback = Vec3.fromXZ(motionX, motionZ).normalize().scale(0.975 * multiplier).addY(0.075 * multiplier)
					
					hitEntity.addVelocity(knockback.x, knockback.y, knockback.z)
					hitEntities.add(hitEntity.uniqueID)
					
					if (hitEntity is EntityPlayer){
						PacketClientLaunchInstantly(hitEntity, hitEntity.motion).sendToPlayer(hitEntity)
					}
				}
			}
		}
	}
}
