package chylex.hee.game.entity.living.ai.util
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.ai.EntityAIWander
import net.minecraft.util.math.Vec3d

abstract class AIBaseWanderConditioned(creature: EntityCreature, speed: Double) : EntityAIWander(creature, speed, 0){
	abstract override fun shouldExecute(): Boolean
	abstract override fun getPosition(): Vec3d?
	
	fun setTarget(newTarget: Vec3d){
		x = newTarget.x
		y = newTarget.y
		z = newTarget.z
	}
	
	override fun makeUpdate(){
		throw UnsupportedOperationException()
	}
	
	override fun setExecutionChance(newChance: Int){
		throw UnsupportedOperationException()
	}
}
