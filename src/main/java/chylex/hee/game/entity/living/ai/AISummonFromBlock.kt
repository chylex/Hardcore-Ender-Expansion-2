package chylex.hee.game.entity.living.ai
import chylex.hee.system.util.AIBase
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.nextInt
import net.minecraft.block.Block
import net.minecraft.entity.EntityCreature
import net.minecraft.world.EnumDifficulty.PEACEFUL

class AISummonFromBlock(
	private val entity: EntityCreature,
	private val searchAttempts: Int,
	private val searchDistance: Int,
	private val searchingFor: Block
) : AIBase(){
	private var summonInTicks = Int.MAX_VALUE
	
	fun triggerSummonInTicks(ticks: Int){
		if (summonInTicks > ticks){
			summonInTicks = ticks.coerceAtLeast(0)
		}
	}
	
	override fun shouldExecute(): Boolean{
		return summonInTicks != Int.MAX_VALUE
	}
	
	override fun updateTask(){
		if (--summonInTicks > 0){
			return
		}
		
		summonInTicks = Int.MAX_VALUE
		
		val world = entity.world
		val difficulty = world.difficulty
		
		if (difficulty == PEACEFUL || !world.gameRules.getBoolean("mobGriefing")){
			return
		}
		
		val rand = entity.rng
		val pos = entity.position
		
		var remainingSpawns = 1 + rand.nextInt(0, difficulty.id)
		
		repeat(searchAttempts){
			val checkedPos = pos.add(
				rand.nextInt(-searchDistance, searchDistance),
				rand.nextInt(-searchDistance / 2, searchDistance / 2),
				rand.nextInt(-searchDistance, searchDistance)
			)
			
			if (checkedPos.getBlock(world) === searchingFor){
				checkedPos.breakBlock(world, true)
				
				if (--remainingSpawns == 0){
					return
				}
			}
		}
	}
}
