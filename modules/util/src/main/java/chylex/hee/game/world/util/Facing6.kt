package chylex.hee.game.world.util

import net.minecraft.util.Direction
import net.minecraft.util.math.vector.Vector3d

object Facing6 : List<Direction> by Direction.values().toList() {
	fun fromDirection(direction: Vector3d): Direction = Direction.getFacingFromVector(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
	fun fromDirection(source: Vector3d, target: Vector3d) = fromDirection(target.subtract(source))
}
