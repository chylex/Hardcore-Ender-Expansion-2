package chylex.hee.game.entity.living.ai

import chylex.hee.system.migration.EntityCreature
import net.minecraft.entity.ai.goal.RandomWalkingGoal
import net.minecraft.util.math.Vec3d

abstract class AIBaseWanderConditioned(creature: EntityCreature, speed: Double) : RandomWalkingGoal(creature, speed, 0) {
	protected inline val entity: EntityCreature
		get() = creature
	
	abstract override fun shouldExecute(): Boolean
	abstract override fun getPosition(): Vec3d?
	
	fun setTarget(newTarget: Vec3d) {
		x = newTarget.x
		y = newTarget.y
		z = newTarget.z
	}
	
	override fun makeUpdate() {
		throw UnsupportedOperationException()
	}
	
	override fun setExecutionChance(newChance: Int) {
		throw UnsupportedOperationException()
	}
}
