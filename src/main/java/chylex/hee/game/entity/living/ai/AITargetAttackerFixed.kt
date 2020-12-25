package chylex.hee.game.entity.living.ai

import chylex.hee.game.entity.posVec
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.migration.EntityLiving
import chylex.hee.system.migration.EntityTameable
import net.minecraft.entity.EntityPredicate
import net.minecraft.entity.ai.goal.Goal.Flag.TARGET
import net.minecraft.entity.ai.goal.TargetGoal
import net.minecraft.util.math.AxisAlignedBB
import java.util.EnumSet

class AITargetAttackerFixed(entity: EntityLiving, private val callReinforcements: Boolean, checkSight: Boolean = true, nearbyOnly: Boolean = false) : TargetGoal(entity, checkSight, nearbyOnly) {
	private val entityPredicate = EntityPredicate().setLineOfSiteRequired().setUseInvisibilityCheck()
	private var revengeTimerOld = 0
	
	init {
		mutexFlags = EnumSet.of(TARGET)
	}
	
	override fun shouldExecute(): Boolean {
		val revengeTimer = goalOwner.revengeTimer
		val revengeTarget = goalOwner.revengeTarget
		
		if (revengeTimer != revengeTimerOld && revengeTarget != null) {
			return isSuitableTarget(revengeTarget, entityPredicate)
		}
		
		return false
	}
	
	override fun startExecuting() {
		target = goalOwner.revengeTarget
		goalOwner.attackTarget = target
		
		revengeTimerOld = goalOwner.revengeTimer
		unseenMemoryTicks = 300
		
		if (callReinforcements) {
			alertOthers()
		}
		
		super.startExecuting()
	}
	
	private fun alertOthers() {
		val maxDistance = targetDistance
		val (x, y, z) = goalOwner.posVec
		
		val friendlies = goalOwner.world.getLoadedEntitiesWithinAABB(goalOwner.javaClass, AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0).grow(maxDistance, 10.0, maxDistance))
		val target = goalOwner.revengeTarget ?: return
		
		for(friendly in friendlies) {
			if (friendly !== goalOwner && friendly.attackTarget == null && (goalOwner !is EntityTameable || goalOwner.owner === (friendly as EntityTameable).owner) && !friendly.isOnSameTeam(target)) {
				friendly.attackTarget = target
			}
		}
	}
}
