package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseTarget
import chylex.hee.system.util.AI_FLAG_NONE
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase

class AITargetSwarmSwitch<T : EntityLivingBase>(
	entity: EntityCreature,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	rangeMultiplier: Float,
	private val targetClass: Class<T>,
	private val targetPredicate: ((T) -> Boolean)?
) : AIBaseTarget(entity, checkSight, easilyReachableOnly, mutexBits = AI_FLAG_NONE){
	private val rangeMultiplier = if (rangeMultiplier in 0F..1F) rangeMultiplier else throw IllegalArgumentException("rangeMultiplier must be between 0 and 1 (inclusive)")
	
	private var triggerRetarget = false
	private var selectedTarget: T? = null
	
	fun triggerRetarget(){
		this.triggerRetarget = true
	}
	
	override fun shouldExecute(): Boolean{
		if (!triggerRetarget){
			return false
		}
		
		triggerRetarget = false
		
		val maxRange = targetDistance
		
		val world = entity.world
		val position = entity.posVec
		val currentTarget = entity.attackTarget
		
		val friendsInRange = world.selectExistingEntities.inRange(entity::class.java, position, maxRange * 2.0).filter { it != entity }
		val friendsAttackingCurrentTarget = friendsInRange.count { it.attackTarget == currentTarget }
		
		if (friendsAttackingCurrentTarget == 0){
			return false
		}
		
		val otherTargetsInRange = world.selectVulnerableEntities.inRange(targetClass, position, maxRange * rangeMultiplier).filter {
			it != currentTarget &&
			isSuitableTarget(it, false) &&
			friendsInRange.count { friend -> friend.attackTarget == it } < friendsAttackingCurrentTarget
		}
		
		val filteredTargets = if (targetPredicate == null) otherTargetsInRange else otherTargetsInRange.filter(targetPredicate)
		
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
