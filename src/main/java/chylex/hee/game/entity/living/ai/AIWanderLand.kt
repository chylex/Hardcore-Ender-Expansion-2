package chylex.hee.game.entity.living.ai
import chylex.hee.system.migration.EntityCreature
import net.minecraft.entity.ai.RandomPositionGenerator
import net.minecraft.entity.ai.goal.RandomWalkingGoal
import net.minecraft.util.math.Vec3d

open class AIWanderLand(
	entity: EntityCreature,
	movementSpeed: Double,
	chancePerTick: Int,
	private val maxDistanceXZ: Int = 10,
	private val maxDistanceY: Int = 7
) : RandomWalkingGoal(entity, movementSpeed, chancePerTick){
	protected inline val entity: EntityCreature
		get() = creature
	
	override fun getPosition(): Vec3d?{
		return RandomPositionGenerator.getLandPos(entity, maxDistanceXZ, maxDistanceY) ?: RandomPositionGenerator.findRandomTarget(entity, maxDistanceXZ, maxDistanceY)
	}
}
