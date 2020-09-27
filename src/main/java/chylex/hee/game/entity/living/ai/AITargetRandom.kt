package chylex.hee.game.entity.living.ai
import chylex.hee.system.migration.EntityCreature
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.random.nextItemOrNull

class AITargetRandom<T : EntityLivingBase>(
	entity: EntityCreature,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	private val chancePerTick: Int
) : AIBaseTargetFiltered<T>(entity, checkSight, easilyReachableOnly, targetClass, targetPredicate){
	override fun findTarget(): T?{
		return if (chancePerTick > 0 && entity.rng.nextInt(chancePerTick) != 0)
			null
		else
			entity.rng.nextItemOrNull(findSuitableTargets())
	}
}
