package chylex.hee.game.entity.living.ai

import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.EntityPredicate
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import java.util.EnumSet

abstract class AIBaseTargetFiltered<T : LivingEntity>(
	entity: CreatureEntity,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	private val targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	mutexBits: EnumSet<Flag> = EnumSet.of(MOVE),
) : AIBaseTarget<T>(entity, checkSight, easilyReachableOnly, mutexBits) {
	private val basicEntityPredicate = EntityPredicate().apply {
		if (!checkSight) {
			setIgnoresLineOfSight()
		}
	}
	
	private val finalTargetPredicate: (T) -> Boolean =
		if (targetPredicate == null)
			{ candidate -> isSuitableTarget(candidate, basicEntityPredicate) }
		else
			{ candidate -> isSuitableTarget(candidate, basicEntityPredicate) && targetPredicate(candidate) }
	
	protected fun findSuitableTargets(rangeMp: Float = 1F): List<T> {
		return entity.world.selectVulnerableEntities.inRange(targetClass, entity.posVec, targetDistance * rangeMp).filter(finalTargetPredicate)
	}
}
