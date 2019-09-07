package chylex.hee.game.entity.living.ai
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase

class AIWanderLandStopNear<T : EntityLivingBase>(
	entity: EntityCreature,
	movementSpeed: Double,
	chancePerTick: Int,
	maxDistanceXZ: Int,
	maxDistanceY: Int,
	private val detectClass: Class<T>,
	private val detectDistance: Double
) : AIWanderLand(entity, movementSpeed, chancePerTick, maxDistanceXZ, maxDistanceY){
	private var nextCheckDelay = 0
	
	override fun updateTask(){
		super.updateTask()
		
		if (--nextCheckDelay < 0){
			nextCheckDelay = 3
			
			if (entity.world.selectVulnerableEntities.inRange(detectClass, entity.posVec, detectDistance).isNotEmpty()){
				entity.navigator.clearPath()
			}
		}
	}
	
	override fun resetTask(){
		super.resetTask()
		nextCheckDelay = 0
	}
}
