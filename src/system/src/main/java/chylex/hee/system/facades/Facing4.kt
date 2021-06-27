package chylex.hee.system.facades

import chylex.hee.system.random.nextItem
import com.google.common.collect.Collections2
import net.minecraft.util.Direction
import net.minecraft.util.math.vector.Vector3d
import java.util.Random

object Facing4 : List<Direction> by Direction.Plane.HORIZONTAL.toList() {
	private val allPermutations: Array<List<Direction>> = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
	
	fun fromDirection(direction: Vector3d): Direction = Direction.getFacingFromVector(direction.x.toFloat(), 0F, direction.z.toFloat())
	fun fromDirection(source: Vector3d, target: Vector3d) = fromDirection(target.subtract(source))
}
