package chylex.hee.game.entity.living.ai

import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.ai.RandomPositionGenerator
import net.minecraft.entity.ai.goal.RandomWalkingGoal
import net.minecraft.util.math.vector.Vector3d

open class AIWanderLand(
	entity: CreatureEntity,
	movementSpeed: Double,
	chancePerTick: Int,
	private val maxDistanceXZ: Int = 10,
	private val maxDistanceY: Int = 7,
) : RandomWalkingGoal(entity, movementSpeed, chancePerTick) {
	protected inline val entity: CreatureEntity
		get() = creature
	
	override fun getPosition(): Vector3d? {
		return RandomPositionGenerator.getLandPos(entity, maxDistanceXZ, maxDistanceY) ?: RandomPositionGenerator.findRandomTarget(entity, maxDistanceXZ, maxDistanceY)
	}
}
