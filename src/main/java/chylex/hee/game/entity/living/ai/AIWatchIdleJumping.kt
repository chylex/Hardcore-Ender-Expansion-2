package chylex.hee.game.entity.living.ai

import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.goal.Goal.Flag.JUMP
import net.minecraft.entity.ai.goal.Goal.Flag.LOOK
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import net.minecraft.entity.ai.goal.LookRandomlyGoal
import java.util.EnumSet

class AIWatchIdleJumping(private val entity: MobEntity, private val chancePerTick: Float = 0.02F, private val delayTicks: Int) : LookRandomlyGoal(entity) {
	private var delayTicksRemaining = 0
	
	init {
		mutexFlags = EnumSet.of(MOVE, LOOK, JUMP)
	}
	
	override fun shouldExecute(): Boolean {
		return entity.isOnGround && entity.rng.nextFloat() < chancePerTick
	}
	
	override fun shouldContinueExecuting(): Boolean {
		return delayTicksRemaining != 0
	}
	
	override fun startExecuting() {
		super.startExecuting()
		delayTicksRemaining = delayTicks
	}
	
	override fun tick() {
		if (delayTicksRemaining > 0) {
			if (!entity.isOnGround) {
				delayTicksRemaining = 0
			}
			else if (--delayTicksRemaining == 0) {
				entity.jumpController.setJumping()
				delayTicksRemaining = -1
			}
		}
		else {
			super.tick()
			
			if (entity.isOnGround) {
				delayTicksRemaining = 0
			}
		}
	}
}
