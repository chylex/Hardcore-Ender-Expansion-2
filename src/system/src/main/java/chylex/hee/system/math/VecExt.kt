@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.math
import net.minecraft.util.math.vector.Vector3d
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

object Vec3{
	/**
	 * Constructs a vector with the provided X/Z values, and `0.0` as the Y value.
	 */
	inline fun fromXZ(x: Double, z: Double): Vector3d{
		return Vector3d(x, 0.0, z)
	}
	
	/**
	 * Constructs a normalized vector from a rotation yaw value in degrees.
	 */
	inline fun fromYaw(yaw: Float): Vector3d{
		return Vector3d.fromPitchYaw(0F, yaw)
	}
}

operator fun Vector3d.component1() = x
operator fun Vector3d.component2() = y
operator fun Vector3d.component3() = z

inline fun Vector3d.scale(factor: Float): Vector3d{
	return this.scale(factor.toDouble())
}

inline fun Vector3d.scale(factor: Int): Vector3d{
	return this.scale(factor.toDouble())
}

fun Vector3d.withY(y: Double): Vector3d{
	return Vector3d(this.x, y, this.z)
}

fun Vector3d.addY(y: Double): Vector3d{
	return Vector3d(this.x, this.y + y, this.z)
}

fun Vector3d.addXZ(x: Double, z: Double): Vector3d{
	return Vector3d(this.x + x, this.y, this.z + z)
}

fun Vector3d.subtractY(y: Double): Vector3d{
	return Vector3d(this.x, this.y - y, this.z)
}

fun Vector3d.scaleXZ(factor: Double): Vector3d{
	return Vector3d(this.x * factor, this.y, this.z * factor)
}

fun Vector3d.scaleY(factor: Double): Vector3d{
	return Vector3d(this.x, this.y * factor, this.z)
}

fun Vector3d.toYaw(): Float{
	return 360F - atan2(this.x, this.z).toDegrees().toFloat()
}

fun Vector3d.toPitch(): Float{
	return -atan2(this.y, sqrt(square(this.x) + square(this.z))).toDegrees().toFloat()
}

fun Vector3d.sign(other: Vector3d): Int{
	return if (this.z * other.x > this.x * other.z) -1 else 1
}

fun Vector3d.angleBetween(other: Vector3d): Double{
	return acos(this.dotProduct(other) / (this.length() * other.length()))
}

fun Vector3d.offsetTowards(other: Vector3d, progress: Double): Vector3d{
	return Vector3d(
		x + (other.x - x) * progress,
		y + (other.y - y) * progress,
		z + (other.z - z) * progress
	)
}

fun Vector3d.directionTowards(target: Vector3d): Vector3d{
	return target.subtract(this).normalize()
}
