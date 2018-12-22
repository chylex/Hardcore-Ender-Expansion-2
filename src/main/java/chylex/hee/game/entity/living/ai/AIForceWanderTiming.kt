package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.ai.util.AIBaseContinuous
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.ai.EntityAIWander

/**
 * Modifies randomness in [EntityAIWander] to prevent the mob from wandering too often (lower bound of [forcedTimingRange]), and force it to wander if it hasn't moved in while (upper bound of [forcedTimingRange]).
 */
class AIForceWanderTiming(private val entity: EntityCreature, private val wanderAI: EntityAIWander, private val defaultChancePerTick: Int, private val forcedTimingRange: IntRange) : AIBaseContinuous(){
	private var ticksSinceLastWalk = 0
	private var lastUpdateTick = 0
	
	override fun tick(){
		if (entity.hasPath()){
			ticksSinceLastWalk = 0
		}
		else{
			ticksSinceLastWalk += entity.ticksExisted - lastUpdateTick
		}
		
		lastUpdateTick = entity.ticksExisted
		wanderAI.setExecutionChance(getCurrentWanderChance())
	}
	
	private fun getCurrentWanderChance(): Int{
		if (ticksSinceLastWalk < forcedTimingRange.start){
			return Int.MAX_VALUE
		}
		
		if (ticksSinceLastWalk in forcedTimingRange){
			return defaultChancePerTick
		}
		
		return 1
	}
}
