package chylex.hee.game.world.feature.basic.caves.impl
import chylex.hee.game.world.feature.basic.caves.ICavePather
import net.minecraft.util.math.vector.Vector3d
import java.util.Random

abstract class CavePatherRotatingBase(initialDirection: Vector3d) : ICavePather{
	protected var direction = initialDirection
		private set
	
	protected var rotation = initialDirection
	
	protected fun setDirectionAndNormalize(newDirection: Vector3d){
		direction = newDirection.normalize()
	}
	
	override fun nextOffset(rand: Random, point: Vector3d, stepSize: Double): Vector3d{
		direction = direction.add(rotation).normalize()
		update(rand, point)
		return direction.scale(stepSize)
	}
	
	abstract fun update(rand: Random, point: Vector3d)
}
