package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.entity.lookPosVec
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.Goal.Flag.LOOK
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import net.minecraft.util.math.Vec3d
import java.util.EnumSet

class AIWatchDyingLeader(
	private val entity: EntityMobBlobby,
	private val ticksBeforeResuming: Int
) : Goal(){
	private var watchTarget = Vec3d.ZERO
	private var remainingTicks = 0
	
	init{
		mutexFlags = EnumSet.of(MOVE, LOOK)
	}
	
	override fun shouldExecute(): Boolean{
		val leader = entity.findLeader()?.takeIf { it.deathTime > 0 } ?: return false
		
		watchTarget = leader.lookPosVec
		remainingTicks = ticksBeforeResuming
		return true
	}
	
	override fun shouldContinueExecuting(): Boolean{
		return remainingTicks > 0
	}
	
	override fun tick(){
		entity.navigator.clearPath()
		entity.lookController.setLookPosition(watchTarget.x, watchTarget.y, watchTarget.z)
		--remainingTicks
	}
	
	override fun resetTask(){
		watchTarget = Vec3d.ZERO
		remainingTicks = 0
	}
}
