package chylex.hee.game.entity.living.ai
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.ai.EntityAIWander
import net.minecraft.entity.ai.RandomPositionGenerator
import net.minecraft.util.math.Vec3d

open class AIWanderLand(
	entity: EntityCreature,
	movementSpeed: Double,
	chancePerTick: Int,
	private val maxDistanceXZ: Int = 10,
	private val maxDistanceY: Int = 7
) : EntityAIWander(entity, movementSpeed, chancePerTick){
	override fun getPosition(): Vec3d?{
		return RandomPositionGenerator.getLandPos(entity, maxDistanceXZ, maxDistanceY) ?: RandomPositionGenerator.findRandomTarget(entity, maxDistanceXZ, maxDistanceY)
	}
}
