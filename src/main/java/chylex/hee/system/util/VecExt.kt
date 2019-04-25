package chylex.hee.system.util
import net.minecraft.util.math.Vec3d
import kotlin.math.acos
import kotlin.math.atan2

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

inline fun Vec3d.scale(factor: Float): Vec3d{
	return this.scale(factor.toDouble())
}

inline fun Vec3d.scale(factor: Int): Vec3d{
	return this.scale(factor.toDouble())
}

fun Vec3d.withY(y: Double): Vec3d{
	return Vec3d(this.x, y, this.z)
}

fun Vec3d.addY(y: Double): Vec3d{
	return Vec3d(this.x, this.y + y, this.z)
}

fun Vec3d.subtractY(y: Double): Vec3d{
	return Vec3d(this.x, this.y - y, this.z)
}

fun Vec3d.scaleY(factor: Double): Vec3d{
	return Vec3d(this.x, this.y * factor, this.z)
}

fun Vec3d.toYaw(): Float{
	return 360F - atan2(this.x, this.z).toDegrees().toFloat()
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
