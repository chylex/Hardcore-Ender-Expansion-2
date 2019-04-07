package chylex.hee.system.util
import net.minecraft.util.math.Vec3d
import kotlin.math.acos

object Vec3{
	/**
	 * Constructs a vector with the provided X/Z values, and `0.0` as the Y value.
	 */
	inline fun fromXZ(x: Double, z: Double): Vec3d{
		return Vec3d(x, 0.0, z)
	}
	
	/**
	 * Constructs a normalized vector from a rotation yaw value in degrees.
	 */
	inline fun fromYaw(yaw: Float): Vec3d{
		return Vec3d.fromPitchYaw(0F, yaw)
	}
}

operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

fun Vec3d.addY(y: Double): Vec3d{
	return Vec3d(this.x, this.y + y, this.z)
}

fun Vec3d.subtractY(y: Double): Vec3d{
	return Vec3d(this.x, this.y - y, this.z)
}

fun Vec3d.angleBetween(other: Vec3d): Double{
	return acos(this.dotProduct(other) / (this.length() * other.length()))
}

fun Vec3d.offsetTowards(other: Vec3d, progress: Double): Vec3d{
	return Vec3d(
		x + (other.x - x) * progress,
		y + (other.y - y) * progress,
		z + (other.z - z) * progress
	)
}
