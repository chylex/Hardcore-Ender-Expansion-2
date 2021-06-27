package chylex.hee.game.entity.living.ai

import chylex.hee.system.migration.EntityCreature
import net.minecraft.entity.ai.RandomPositionGenerator
import net.minecraft.entity.ai.goal.RandomWalkingGoal
import net.minecraft.util.math.vector.Vector3d

class AIWander(
	entity: EntityCreature,
	movementSpeed: Double,
	chancePerTick: Int,
	private val maxDistanceXZ: Int = 10,
	private val maxDistanceY: Int = 7,
) : RandomWalkingGoal(entity, movementSpeed, chancePerTick) {
	override fun getPosition(): Vector3d? {
		return RandomPositionGenerator.findRandomTarget(creature, maxDistanceXZ, maxDistanceY)
	}
}
