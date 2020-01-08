package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.migration.vanilla.EntityCreature
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.EntityPredicate
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import java.util.EnumSet

abstract class AIBaseTargetFiltered<T : EntityLivingBase>(
	entity: EntityCreature,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	private val targetClass: Class<T>,
	targetPredicate: ((T) -> Boolean)?,
	mutexBits: EnumSet<Flag> = EnumSet.of(MOVE)
) : AIBaseTarget<T>(entity, checkSight, easilyReachableOnly, mutexBits){
	private val basicEntityPredicate = EntityPredicate().apply {
		if (checkSight){
			setLineOfSiteRequired()
		}
	}
	
	private val finalTargetPredicate: (T) -> Boolean =
		if (targetPredicate == null)
			{ candidate -> isSuitableTarget(candidate, basicEntityPredicate) }
		else
			{ candidate -> isSuitableTarget(candidate, basicEntityPredicate) && targetPredicate(candidate) }
	
	protected fun findSuitableTargets(rangeMp: Float = 1F): List<T>{
		return entity.world.selectVulnerableEntities.inRange(targetClass, entity.posVec, targetDistance * rangeMp).filter(finalTargetPredicate)
	}
}
