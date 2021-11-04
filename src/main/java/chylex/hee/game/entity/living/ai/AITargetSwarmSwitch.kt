package chylex.hee.game.entity.living.ai

import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.util.random.nextItemOrNull
import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.LivingEntity
import java.util.EnumSet

class AITargetSwarmSwitch<T : LivingEntity>(
	entity: CreatureEntity,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	rangeMultiplier: Float,
) : AIBaseTargetFiltered<T>(entity, checkSight, easilyReachableOnly, targetClass, targetPredicate, mutexBits = EnumSet.noneOf(Flag::class.java)) {
	private val rangeMultiplier = if (rangeMultiplier in 0F..1F) rangeMultiplier else throw IllegalArgumentException("rangeMultiplier must be between 0 and 1 (inclusive)")
	
	private var triggerRetarget = false
	
	fun triggerRetarget() {
		this.triggerRetarget = true
	}
	
	override fun findTarget(): T? {
		if (!triggerRetarget) {
			return null
		}
		
		triggerRetarget = false
		
		val maxRange = targetDistance
		
		val world = entity.world
		val position = entity.posVec
		val currentTarget = entity.attackTarget
		
		val friendsInRange = world.selectExistingEntities.inRange(entity::class.java, position, maxRange * 2.0).filter { it != entity }
		val friendsAttackingCurrentTarget = friendsInRange.count { it.attackTarget === currentTarget }
		
		if (friendsAttackingCurrentTarget == 0) {
			return null
		}
		
		val otherTargetsInRange = findSuitableTargets(rangeMultiplier).filter {
			it != currentTarget &&
			friendsInRange.count { friend -> friend.attackTarget === it } < friendsAttackingCurrentTarget
		}
		
		return entity.rng.nextItemOrNull(otherTargetsInRange)
	}
}
