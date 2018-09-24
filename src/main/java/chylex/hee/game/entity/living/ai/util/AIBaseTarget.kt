package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.util.AI_FLAG_MOVEMENT
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.ai.EntityAITarget

abstract class AIBaseTarget(entity: EntityCreature, checkSight: Boolean, easilyReachableOnly: Boolean, mutexBits: Int = AI_FLAG_MOVEMENT) : EntityAITarget(entity, checkSight, easilyReachableOnly){
	init{
		this.mutexBits = mutexBits
	}
	
	protected inline val entity: EntityCreature
		get() = taskOwner
}
