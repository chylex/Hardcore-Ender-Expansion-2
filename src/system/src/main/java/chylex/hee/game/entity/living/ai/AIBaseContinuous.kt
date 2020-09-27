package chylex.hee.game.entity.living.ai
import net.minecraft.entity.ai.goal.Goal

abstract class AIBaseContinuous : Goal(){
	final override fun shouldExecute(): Boolean{
		return true
	}
	
	final override fun shouldContinueExecuting(): Boolean{
		return true
	}
	
	final override fun startExecuting(){}
	final override fun resetTask(){}
}
