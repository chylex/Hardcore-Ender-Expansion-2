package chylex.hee.game.entity.living.behavior

import chylex.hee.game.entity.damage.IDamageDealer
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.MobTypes
import chylex.hee.game.entity.util.lookDirVec
import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.game.entity.util.motionX
import chylex.hee.game.entity.util.motionZ
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.addY
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.square
import chylex.hee.util.math.toRadians
import chylex.hee.util.math.withY
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.AxisAlignedBB
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow

sealed class EnderEyeAttack {
	abstract val dealtDamageType: IDamageDealer
	abstract val dealtDamageMultiplier: Float
	abstract val dealtKnockbackMultiplier: Float
	abstract val canTakeKnockback: Boolean
	abstract fun tick(entity: EntityBossEnderEye): Boolean
	
	class Melee : EnderEyeAttack() {
		override val dealtDamageType
			get() = EntityBossEnderEye.DAMAGE_MELEE
		
		override val dealtDamageMultiplier
			get() = 1F
		
		override val dealtKnockbackMultiplier
			get() = 1F
		
		override val canTakeKnockback = true
		private var currentSpeed = 1.0
		private var lastAttackTime = 0L
		private var laserEyeTicksRemaining = 260
		
		override fun tick(entity: EntityBossEnderEye): Boolean = with(entity) {
			val target = attackTarget ?: forceFindNewTarget()
			
			if (target == null) {
				armPosition = EntityBossEnderEye.ARMS_LIMP
				aiMoveSpeed = 0F
				setRotateTarget(null)
			}
			else {
				armPosition = EntityBossEnderEye.ARMS_HUG
				lookController.setLookPositionWithEntity(target, 0F, 0F)
				setRotateTarget(target)
				
				val currentVec = lookPosVec
				val targetVec = target.lookPosVec
				val distSq = targetVec.squareDistanceTo(currentVec)
				
				if (distSq > square(6.0 - ((currentSpeed - 1.0) / 1.5))) {
					if (currentSpeed < 3.5) {
						currentSpeed = (currentSpeed + 0.1).coerceAtMost(3.5)
					}
					
					navigator.tryMoveToXYZ(targetVec.x, targetVec.y + (currentSpeed - 1.0) * 0.6, targetVec.z, currentSpeed)
				}
				else if (distSq > square(1.2)) {
					if (currentSpeed > 1.0) {
						currentSpeed = (currentSpeed - 0.3).coerceAtLeast(1.0)
					}
					
					navigator.tryMoveToXYZ(targetVec.x, targetVec.y, targetVec.z, currentSpeed)
				}
				else {
					if (currentVec.directionTowards(targetVec).dotProduct(motion.normalize()) > 0.0) {
						motion = motion.scale(0.4)
					}
				}
				
				if (distSq < square(1.4)) {
					val currentTime = world.gameTime
					
					if (currentTime - lastAttackTime >= 20L) {
						lastAttackTime = currentTime
						attackEntityAsMob(target)
					}
				}
				
				if (--laserEyeTicksRemaining == 0) {
					laserEyeTicksRemaining = rng.nextInt(160, 260)
					return false
				}
			}
			
			return true
		}
		
		fun reset(entity: EntityBossEnderEye) {
			currentSpeed = 1.0
			lastAttackTime = entity.world.gameTime
			laserEyeTicksRemaining = laserEyeTicksRemaining.coerceAtLeast(35)
		}
	}
	
	class LaserEye : EnderEyeAttack() {
		override val dealtDamageType
			get() = EntityBossEnderEye.DAMAGE_LASER
		
		override val dealtDamageMultiplier
			get() = 0.75F
		
		override val dealtKnockbackMultiplier
			get() = 0F
		
		override val canTakeKnockback = true
		
		private var closedEyeTimer = 18
		private var laserTicksLeft = 210
		private var rotationSpeedTimer = 0
		private var hasSwitchedTarget = false
		
		override fun tick(entity: EntityBossEnderEye): Boolean = with(entity) {
			if (closedEyeTimer > 0) {
				--closedEyeTimer
				armPosition = EntityBossEnderEye.ARMS_LIMP
				eyeState = EntityBossEnderEye.EYE_CLOSED
				navigator.clearPath()
				setRotateTarget(null)
				return true
			}
			
			if (laserTicksLeft == 0) {
				eyeState = EntityBossEnderEye.EYE_OPEN
				return false
			}
			
			val target = attackTarget ?: tryFindNewTarget()
			
			if (target == null || --laserTicksLeft <= rng.nextInt(0, 30)) {
				laserTicksLeft = 0
				closedEyeTimer = 15
				setRotateTarget(null)
				return true
			}
			
			if (rng.nextInt(100) < health * 0.2F) {
				--laserTicksLeft
			}
			
			armPosition = EntityBossEnderEye.ARMS_ATTACK
			eyeState = EntityBossEnderEye.EYE_LASER
			setRotateTarget(target, 0.01F + ((rotationSpeedTimer++) * 0.0045F).pow(1.666F).coerceAtMost(0.59F))
			
			val laserStart = lookPosVec
			val laserEnd = getLaserHit(1F)
			val laserLen = laserEnd.distanceTo(laserStart)
			
			for (testEntity in world.selectVulnerableEntities.inBox<LivingEntity>(boundingBox.grow(laserLen))) {
				if (testEntity.boundingBox.rayTrace(laserStart, laserEnd).isPresent) {
					attackEntityAsMob(testEntity)
				}
			}
			
			if (!canEntityBeSeen(target)) {
				laserTicksLeft -= 4
				
				if (!hasSwitchedTarget) {
					hasSwitchedTarget = true
					tryFindNewTarget()
				}
			}
			
			return true
		}
		
		private fun EntityBossEnderEye.tryFindNewTarget(): LivingEntity? {
			val prevTarget = attackTarget
			
			if (forceFindNewTarget() == null) {
				attackTarget = prevTarget
				return null // used to reset the attack if the player gets too far with no other nearby targets
			}
			
			return prevTarget
		}
	}
	
	class KnockbackDash : EnderEyeAttack() {
		override val dealtDamageType
			get() = EntityBossEnderEye.DAMAGE_DASH
		
		override val dealtDamageMultiplier
			get() = 2F
		
		override val dealtKnockbackMultiplier
			get() = 1F // additional knockback is dealt manually per entity
		
		override val canTakeKnockback = false
		
		private var isSlowingDown = true
		private var attackTimer = 0
		private var attackRepeats = 0
		private val hitEntities = mutableSetOf<UUID>()
		
		override fun tick(entity: EntityBossEnderEye): Boolean = with(entity) {
			val target = attackTarget ?: return false
			
			if (isSlowingDown) {
				armPosition = EntityBossEnderEye.ARMS_ATTACK
				aiMoveSpeed = 0F
				setRotateTarget(target)
				
				if (motion.withY(0.0).lengthSquared() < square(0.05)) {
					if (attackRepeats == 0) {
						isSlowingDown = false
					}
					else {
						val lookDir = lookDirVec
						val targetDir = target.lookPosVec.subtract(lookPosVec).normalize()
						
						if (abs(lookDir.dotProduct(targetDir)) > cos(10.0.toRadians())) {
							isSlowingDown = false
						}
					}
				}
			}
			else {
				armPosition = EntityBossEnderEye.ARMS_HUG
				setRotateTarget(null)
				
				if (attackTimer == 0) {
					motion = target.lookPosVec.subtract(lookPosVec).scale(rng.nextFloat(0.15, 0.18))
					attackTimer = 1
				}
				
				if (hitEntities.isNotEmpty()) {
					++attackTimer
				}
				
				if (attackTimer == 24 || motion.withY(0.0).lengthSquared() < square(0.15)) {
					if (health < realMaxHealth * 0.5F && attackRepeats == 0) {
						++attackRepeats
						isSlowingDown = true
						attackTimer = 0
						hitEntities.clear()
						return true
					}
					
					return false
				}
				
				causeDamageInFront(this)
			}
			
			return true
		}
		
		private fun causeDamageInFront(entity: EntityBossEnderEye) = with(entity) {
			val frontHurtCenter = lookPosVec.add(lookDirVec.scale(width * 0.75))
			val frontHurtDist = width * 0.6
			
			for (hitEntity in world.selectVulnerableEntities.inBox<LivingEntity>(AxisAlignedBB(frontHurtCenter, frontHurtCenter).grow(frontHurtDist))) {
				if (!MobTypes.isBoss(hitEntity) && !hitEntities.contains(hitEntity.uniqueID) && attackEntityAsMob(hitEntity)) {
					val multiplier = when {
						hitEntity.isActiveItemStackBlocking -> 0.25
						hitEntity.isSneaking                -> 0.75
						else                                -> 1.0
					}
					
					val knockback = Vec3.xz(motionX, motionZ).normalize().scale(0.975 * multiplier).addY(0.075 * multiplier)
					
					hitEntity.addVelocity(knockback.x, knockback.y, knockback.z)
					hitEntities.add(hitEntity.uniqueID)
					
					if (hitEntity is PlayerEntity) {
						PacketClientLaunchInstantly(hitEntity, hitEntity.motion).sendToPlayer(hitEntity)
					}
				}
			}
		}
	}
}
