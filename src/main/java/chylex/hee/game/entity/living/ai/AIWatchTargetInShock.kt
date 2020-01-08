package chylex.hee.game.entity.living.ai
import chylex.hee.system.migration.vanilla.EntityCreature
import chylex.hee.system.util.square
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.Goal.Flag.LOOK
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import java.util.EnumSet
import kotlin.math.max

class AIWatchTargetInShock(
	private val entity: EntityCreature,
	maxDistance: Double
) : Goal(){
	val isWatching
		get() = remainingTicks > 0
	
	private val maxDistanceSq = square(maxDistance)
	private var remainingTicks = 0
	
	init{
		mutexFlags = EnumSet.of(MOVE, LOOK)
	}
	
	fun startWatching(ticks: Int){
		remainingTicks = max(0, ticks)
	}
	
	fun stopWatching(){
		remainingTicks = 0
	}
	
	override fun shouldExecute(): Boolean{
		return remainingTicks > 0
	}
	
	override fun shouldContinueExecuting(): Boolean{
		if (remainingTicks == 0){
			return false
		}
		
		val target = entity.attackTarget
		return target != null && target.isAlive && entity.getDistanceSq(target) <= maxDistanceSq
	}
	
	override fun tick(){
		val target = entity.attackTarget ?: return
		
		entity.lookController.setLookPosition(target.posX, target.posY + target.eyeHeight, target.posZ, entity.horizontalFaceSpeed.toFloat(), entity.verticalFaceSpeed.toFloat())
		--remainingTicks
	}
	
	override fun resetTask(){
		remainingTicks = 0
	}
}
