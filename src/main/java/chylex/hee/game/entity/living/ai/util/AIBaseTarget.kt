package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.util.AI_FLAG_MOVEMENT
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.ai.EntityAITarget

abstract class AIBaseTarget<T : EntityLivingBase>(entity: EntityCreature, checkSight: Boolean, easilyReachableOnly: Boolean, mutexBits: Int = AI_FLAG_MOVEMENT) : EntityAITarget(entity, checkSight, easilyReachableOnly){
	init{
		this.mutexBits = mutexBits
	}
	
	protected inline val entity: EntityCreature
		get() = taskOwner
	
	private var selectedTarget: T? = null
	
	abstract fun findTarget(): T?
	
	final override fun shouldExecute(): Boolean{
		val newTarget = findTarget()
		
		if (newTarget == null){
			return false
		}
		
		selectedTarget = newTarget
		return true
	}
	
	final override fun startExecuting(){
		entity.attackTarget = selectedTarget
		super.startExecuting()
	}
	
	final override fun resetTask(){
		selectedTarget = null
		super.resetTask()
	}
}
