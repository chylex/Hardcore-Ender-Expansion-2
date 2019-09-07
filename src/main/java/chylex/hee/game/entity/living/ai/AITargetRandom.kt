package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseTargetFiltered
import chylex.hee.system.util.nextItemOrNull
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase

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
