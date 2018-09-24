package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseTarget
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase

class AITargetRandom_<T : EntityLivingBase>(
	entity: EntityCreature,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	private val chancePerTick: Int,
	private val targetClass: Class<T>,
	private val targetPredicate: ((T) -> Boolean)?
) : AIBaseTarget(entity, checkSight, easilyReachableOnly){
	private var selectedTarget: T? = null
	
	override fun shouldExecute(): Boolean{
		if (chancePerTick > 0 && entity.rng.nextInt(chancePerTick) != 0){
			return false
		}
		
		val targetsInRange = entity.world.selectVulnerableEntities.inRange(targetClass, entity.posVec, targetDistance).filter { isSuitableTarget(it, false) }
		val filteredTargets = if (targetPredicate == null) targetsInRange else targetsInRange.filter(targetPredicate)
		
		selectedTarget = entity.rng.nextItem(filteredTargets.toList())
		return selectedTarget != null
	}
	
	override fun startExecuting(){
		entity.attackTarget = selectedTarget
		super.startExecuting()
	}
	
	override fun resetTask(){
		selectedTarget = null
		super.resetTask()
	}
}
