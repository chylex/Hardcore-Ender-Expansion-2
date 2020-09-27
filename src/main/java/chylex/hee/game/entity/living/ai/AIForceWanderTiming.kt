package chylex.hee.game.entity.living.ai
import chylex.hee.system.migration.EntityCreature
import net.minecraft.entity.ai.goal.RandomWalkingGoal

/**
 * Modifies randomness in [RandomWalkingGoal] to prevent the mob from wandering too often (lower bound of [forcedTimingRange]), and force it to wander if it hasn't moved in while (upper bound of [forcedTimingRange]).
 */
class AIForceWanderTiming(
	private val entity: EntityCreature,
	private val wanderAI: RandomWalkingGoal,
	private val defaultChancePerTick: Int,
	private val forcedTimingRange: IntRange
) : AIBaseContinuous(){
	private var ticksSinceLastWalk = 0
	
	override fun tick(){
		if (entity.hasPath()){
			ticksSinceLastWalk = 0
		}
		else{
			++ticksSinceLastWalk
		}
		
		wanderAI.setExecutionChance(getCurrentWanderChance())
	}
	
	private fun getCurrentWanderChance(): Int{
		if (ticksSinceLastWalk < forcedTimingRange.first){
			return Int.MAX_VALUE
		}
		
		if (ticksSinceLastWalk in forcedTimingRange){
			return defaultChancePerTick
		}
		
		return 1
	}
}
