package chylex.hee.game.entity.living.ai
import chylex.hee.system.migration.EntityCreature
import chylex.hee.system.migration.EntityLiving
import chylex.hee.system.migration.EntityLivingBase
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import net.minecraft.entity.ai.goal.TargetGoal
import java.util.EnumSet

abstract class AIBaseTarget<T : EntityLivingBase>(
	entity: EntityCreature,
	checkSight: Boolean,
	easilyReachableOnly: Boolean,
	mutexBits: EnumSet<Flag> = EnumSet.of(MOVE)
) : TargetGoal(entity, checkSight, easilyReachableOnly){
	init{
		mutexFlags = mutexBits
	}
	
	protected inline val entity: EntityLiving
		get() = goalOwner
	
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
