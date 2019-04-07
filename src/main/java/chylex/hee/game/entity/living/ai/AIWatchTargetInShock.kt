package chylex.hee.game.entity.living.ai
import chylex.hee.system.util.AIBase
import chylex.hee.system.util.AI_FLAG_LOOKING
import chylex.hee.system.util.AI_FLAG_MOVEMENT
import chylex.hee.system.util.square
import net.minecraft.entity.EntityCreature
import kotlin.math.max

class AIWatchTargetInShock(
	private val entity: EntityCreature,
	maxDistance: Double
) : AIBase(){
	val isWatching
		get() = remainingTicks > 0
	
	private val maxDistanceSq = square(maxDistance)
	private var remainingTicks = 0
	
	init{
		this.mutexBits = AI_FLAG_LOOKING or AI_FLAG_MOVEMENT
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
		return target != null && target.isEntityAlive && entity.getDistanceSq(target) <= maxDistanceSq
	}
	
	override fun updateTask(){
		val target = entity.attackTarget ?: return
		
		entity.lookHelper.setLookPosition(target.posX, target.posY + target.eyeHeight, target.posZ, entity.horizontalFaceSpeed.toFloat(), entity.verticalFaceSpeed.toFloat())
		--remainingTicks
	}
	
	override fun resetTask(){
		remainingTicks = 0
	}
}
