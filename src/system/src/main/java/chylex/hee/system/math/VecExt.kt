@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.math

import net.minecraft.util.math.Vec3d
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

inline fun Vec(x: Double, y: Double, z: Double) = Vec3d(x, y, z)

object Vec3 {
	val ZERO: Vec3d = Vec3d.ZERO
	
	inline fun xz(x: Double, z: Double): Vec3d {
		return Vec(x, 0.0, z)
	}
	
	inline fun y(y: Double): Vec3d {
		return Vec(0.0, y, 0.0)
	}
	
	inline fun y(y: Int): Vec3d {
		return Vec(0.0, y.toDouble(), 0.0)
	}
	
	inline fun xyz(xyz: Double): Vec3d {
		return Vec(xyz, xyz, xyz)
	}
	
	inline fun fromYaw(yaw: Float): Vec3d {
		return Vec3d.fromPitchYaw(0F, yaw)
	}
}

operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

inline fun Vec3d.scale(factor: Float): Vec3d {
	return this.scale(factor.toDouble())
}

inline fun Vec3d.scale(factor: Int): Vec3d {
	return this.scale(factor.toDouble())
}

fun Vec3d.withY(y: Double): Vec3d {
	return Vec(this.x, y, this.z)
}

fun Vec3d.addY(y: Double): Vec3d {
	return Vec(this.x, this.y + y, this.z)
}

fun Vec3d.addXZ(x: Double, z: Double): Vec3d {
	return Vec(this.x + x, this.y, this.z + z)
}

fun Vec3d.subtractY(y: Double): Vec3d {
	return Vec(this.x, this.y - y, this.z)
}

fun Vec3d.scaleXZ(factor: Double): Vec3d {
	return Vec(this.x * factor, this.y, this.z * factor)
}

fun Vec3d.scaleY(factor: Double): Vec3d {
	return Vec(this.x, this.y * factor, this.z)
}

fun Vec3d.toYaw(): Float {
	return 360F - atan2(this.x, this.z).toDegrees().toFloat()
}

fun Vec3d.toPitch(): Float {
	return -atan2(this.y, sqrt(square(this.x) + square(this.z))).toDegrees().toFloat()
}

fun Vec3d.sign(other: Vec3d): Int {
	return if (this.z * other.x > this.x * other.z) -1 else 1
}

fun Vec3d.angleBetween(other: Vec3d): Double {
	return acos(this.dotProduct(other) / (this.length() * other.length()))
}

fun Vec3d.offsetTowards(other: Vec3d, progress: Double): Vec3d {
	return Vec(
		x + (other.x - x) * progress,
		y + (other.y - y) * progress,
		z + (other.z - z) * progress
	)
}

fun Vec3d.directionTowards(target: Vec3d): Vec3d {
	return target.subtract(this).normalize()
}
