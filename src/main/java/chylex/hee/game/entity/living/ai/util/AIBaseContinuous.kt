package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.util.AIBase
import chylex.hee.system.util.AI_FLAG_NONE

abstract class AIBaseContinuous : AIBase(){
	init{
		this.mutexBits = AI_FLAG_NONE
	}
	
	protected abstract fun tick()
	
	final override fun shouldExecute(): Boolean{
		return true
	}
	
	final override fun shouldContinueExecuting(): Boolean{
		return true
	}
	
	final override fun startExecuting(){}
	final override fun updateTask() = tick()
	final override fun resetTask(){}
}
