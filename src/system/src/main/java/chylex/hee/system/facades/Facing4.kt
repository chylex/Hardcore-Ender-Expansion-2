package chylex.hee.system.facades

import chylex.hee.system.random.nextItem
import com.google.common.collect.Collections2
import net.minecraft.util.Direction
import net.minecraft.util.math.Vec3d
import java.util.Random

object Facing4 : List<Direction> by Direction.Plane.HORIZONTAL.toList() {
	private val allPermutations: Array<List<Direction>> = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
	
	fun fromDirection(direction: Vec3d): Direction = Direction.getFacingFromVector(direction.x.toFloat(), 0F, direction.z.toFloat())
	fun fromDirection(source: Vec3d, target: Vec3d) = fromDirection(target.subtract(source))
}
