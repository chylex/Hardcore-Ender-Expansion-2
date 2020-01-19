package chylex.hee.game.entity.living.ai
import chylex.hee.system.migration.vanilla.EntityCreature
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isPeaceful
import chylex.hee.system.util.nextInt
import net.minecraft.block.Block
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.world.GameRules.MOB_GRIEFING

class AISummonFromBlock(
	private val entity: EntityCreature,
	private val searchAttempts: Int,
	private val searchDistance: Int,
	private val searchingFor: (Block) -> Boolean
) : Goal(){
	private var summonInTicks = Int.MAX_VALUE
	
	fun triggerSummonInTicks(ticks: Int){
		if (summonInTicks > ticks){
			summonInTicks = ticks.coerceAtLeast(0)
		}
	}
	
	override fun shouldExecute(): Boolean{
		return summonInTicks != Int.MAX_VALUE
	}
	
	override fun tick(){
		if (--summonInTicks > 0){
			return
		}
		
		summonInTicks = Int.MAX_VALUE
		
		val world = entity.world
		
		if (world.isPeaceful || !world.gameRules.getBoolean(MOB_GRIEFING)){
			return
		}
		
		val rand = entity.rng
		val pos = entity.position
		
		var remainingSpawns = 1 + rand.nextInt(0, world.difficulty.id)
		
		repeat(searchAttempts){
			val checkedPos = pos.add(
				rand.nextInt(-searchDistance, searchDistance),
				rand.nextInt(-searchDistance / 2, searchDistance / 2),
				rand.nextInt(-searchDistance, searchDistance)
			)
			
			if (searchingFor(checkedPos.getBlock(world))){
				checkedPos.breakBlock(world, true)
				
				if (--remainingSpawns == 0){
					return
				}
			}
		}
	}
}
