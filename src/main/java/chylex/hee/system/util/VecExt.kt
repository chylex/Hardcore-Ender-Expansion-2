package chylex.hee.system.util
import net.minecraft.util.math.Vec3d
import kotlin.math.acos

inline fun Vec3d.add(x: Double, y: Double, z: Double): Vec3d{ // UPDATE: remove if the name was changed
	return this.addVector(x, y, z)
}

inline fun Vec3d.length(): Double{ // UPDATE: remove if the name was changed
	return this.lengthVector()
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
