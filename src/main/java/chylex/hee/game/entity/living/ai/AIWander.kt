package chylex.hee.game.entity.living.ai
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.ai.EntityAIWander
import net.minecraft.entity.ai.RandomPositionGenerator
import net.minecraft.util.math.Vec3d

class AIWander(
	entity: EntityCreature,
	movementSpeed: Double,
	chancePerTick: Int,
	private val maxDistanceXZ: Int = 10,
	private val maxDistanceY: Int = 7
) : EntityAIWander(entity, movementSpeed, chancePerTick){
	override fun getPosition(): Vec3d?{
		return RandomPositionGenerator.findRandomTarget(entity, maxDistanceXZ, maxDistanceY)
	}
}
