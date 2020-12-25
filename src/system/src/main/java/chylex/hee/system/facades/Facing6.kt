package chylex.hee.system.facades

import net.minecraft.util.Direction
import net.minecraft.util.math.Vec3d

object Facing6 : List<Direction> by Direction.values().toList() {
	fun fromDirection(direction: Vec3d): Direction = Direction.getFacingFromVector(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
	fun fromDirection(source: Vec3d, target: Vec3d) = fromDirection(target.subtract(source))
}
