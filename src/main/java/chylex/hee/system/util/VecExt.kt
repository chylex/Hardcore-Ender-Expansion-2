package chylex.hee.system.util
import net.minecraft.util.math.Vec3d
import kotlin.math.acos

operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

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
