package chylex.hee.game.world.feature.basic.caves.impl

import chylex.hee.game.world.feature.basic.caves.ICavePather
import net.minecraft.util.math.Vec3d
import java.util.Random

abstract class CavePatherRotatingBase(initialDirection: Vec3d) : ICavePather {
	protected var direction = initialDirection
		private set
	
	protected var rotation = initialDirection
	
	protected fun setDirectionAndNormalize(newDirection: Vec3d) {
		direction = newDirection.normalize()
	}
	
	override fun nextOffset(rand: Random, point: Vec3d, stepSize: Double): Vec3d {
		direction = direction.add(rotation).normalize()
		update(rand, point)
		return direction.scale(stepSize)
	}
	
	abstract fun update(rand: Random, point: Vec3d)
}
