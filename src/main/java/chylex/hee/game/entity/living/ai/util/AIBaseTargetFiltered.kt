package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.util.AI_FLAG_MOVEMENT
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase

abstract class AIBaseTargetFiltered<T : EntityLivingBase>(
	entity: EntityCreature,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	private val targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	mutexBits: Int = AI_FLAG_MOVEMENT
) : AIBaseTarget<T>(entity, checkSight, easilyReachableOnly, mutexBits){
	private val finalTargetPredicate: (T) -> Boolean =
		if (targetPredicate == null)
			{ candidate -> isSuitableTarget(candidate, false) }
		else
			{ candidate -> isSuitableTarget(candidate, false) && targetPredicate(candidate) }
	
	protected fun findSuitableTargets(rangeMp: Float = 1F): List<T>{
		return entity.world.selectVulnerableEntities.inRange(targetClass, entity.posVec, targetDistance * rangeMp).filter(finalTargetPredicate)
	}
}
