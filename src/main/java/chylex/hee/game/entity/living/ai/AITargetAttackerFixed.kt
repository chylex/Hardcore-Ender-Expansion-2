package chylex.hee.game.entity.living.ai
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.posVec
import net.minecraft.entity.EntityPredicate
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.goal.Goal.Flag.TARGET
import net.minecraft.entity.ai.goal.TargetGoal
import net.minecraft.entity.passive.TameableEntity
import net.minecraft.util.math.AxisAlignedBB
import java.util.EnumSet

class AITargetAttackerFixed(entity: MobEntity, private val callReinforcements: Boolean, checkSight: Boolean = true, nearbyOnly: Boolean = false) : TargetGoal(entity, checkSight, nearbyOnly){
	private val entityPredicate = EntityPredicate().setLineOfSiteRequired().setUseInvisibilityCheck()
	private var revengeTimerOld = 0
	
	init{
		mutexFlags = EnumSet.of(TARGET)
	}
	
	override fun shouldExecute(): Boolean{
		val revengeTimer = goalOwner.revengeTimer
		val revengeTarget = goalOwner.revengeTarget
		
		if (revengeTimer != revengeTimerOld && revengeTarget != null){
			return isSuitableTarget(revengeTarget, entityPredicate)
		}
		
		return false
	}
	
	override fun startExecuting(){
		target = goalOwner.revengeTarget
		goalOwner.attackTarget = target
		
		revengeTimerOld = goalOwner.revengeTimer
		unseenMemoryTicks = 300
		
		if (callReinforcements){
			alertOthers()
		}
		
		super.startExecuting()
	}
	
	private fun alertOthers(){
		val maxDistance = targetDistance
		val (x, y, z) = goalOwner.posVec
		
		val friendlies = goalOwner.world.getLoadedEntitiesWithinAABB(goalOwner.javaClass, AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0).grow(maxDistance, 10.0, maxDistance))
		val target = goalOwner.revengeTarget ?: return
		
		for(friendly in friendlies){
			if (friendly !== goalOwner && friendly.attackTarget == null && (goalOwner !is TameableEntity || goalOwner.owner === (friendly as TameableEntity).owner) && !friendly.isOnSameTeam(target)){
				friendly.attackTarget = target
			}
		}
	}
}
