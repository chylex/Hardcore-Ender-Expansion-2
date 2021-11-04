package chylex.hee.game.entity.living.ai

import chylex.hee.util.random.nextItemOrNull
import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.LivingEntity

class AITargetRandom<T : LivingEntity>(
	entity: CreatureEntity,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	private val chancePerTick: Int,
) : AIBaseTargetFiltered<T>(entity, checkSight, easilyReachableOnly, targetClass, targetPredicate) {
	override fun findTarget(): T? {
		return if (chancePerTick > 0 && entity.rng.nextInt(chancePerTick) != 0)
			null
		else
			entity.rng.nextItemOrNull(findSuitableTargets())
	}
}
