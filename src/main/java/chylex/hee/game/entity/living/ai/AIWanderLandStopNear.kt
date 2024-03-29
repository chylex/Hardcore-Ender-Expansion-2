package chylex.hee.game.entity.living.ai

import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.LivingEntity

class AIWanderLandStopNear<T : LivingEntity>(
	entity: CreatureEntity,
	movementSpeed: Double,
	chancePerTick: Int,
	maxDistanceXZ: Int,
	maxDistanceY: Int,
	private val detectClass: Class<T>,
	private val detectDistance: Double,
) : AIWanderLand(entity, movementSpeed, chancePerTick, maxDistanceXZ, maxDistanceY) {
	private var nextCheckDelay = 0
	
	override fun tick() {
		super.tick()
		
		if (--nextCheckDelay < 0) {
			nextCheckDelay = 3
			
			if (entity.world.selectVulnerableEntities.inRange(detectClass, entity.posVec, detectDistance).isNotEmpty()) {
				entity.navigator.clearPath()
			}
		}
	}
	
	override fun resetTask() {
		super.resetTask()
		nextCheckDelay = 0
	}
}
