package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.util.AIBase
import chylex.hee.system.util.AI_FLAG_NONE

abstract class AIBaseContinuous(mutexBits: Int = AI_FLAG_NONE) : AIBase(){
	init{
		this.mutexBits = mutexBits
	}
	
	protected abstract fun tick()
	
	final override fun shouldExecute(): Boolean{
		tick()
		return false
	}
	
	final override fun shouldContinueExecuting(): Boolean{
		return false
	}
	
	final override fun startExecuting(){}
	final override fun updateTask(){}
	final override fun resetTask(){}
}
