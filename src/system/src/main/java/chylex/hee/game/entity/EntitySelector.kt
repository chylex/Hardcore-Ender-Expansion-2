package chylex.hee.game.entity

import chylex.hee.system.math.square
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IEntityReader
import java.util.function.Predicate

@Suppress("NOTHING_TO_INLINE")
class EntitySelector(private val world: IEntityReader, private val predicate: Predicate<in Entity>) {
	companion object {
		val INFINITE_AABB = AxisAlignedBB(
			Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
		)
	}
	
	fun <T : Entity> inBox(cls: Class<T>, aabb: AxisAlignedBB): List<T> {
		return world.getEntitiesWithinAABB(cls, aabb, predicate)
	}
	
	fun <T : Entity> inRange(cls: Class<T>, point: Vec3d, range: Double): List<T> {
		val aabb = AxisAlignedBB(point.x - range, point.y - range, point.z - range, point.x + range, point.y + range, point.z + range)
		val rangeSq = square(range)
		
		return world.getEntitiesWithinAABB(cls, aabb) { predicate.test(it) && it!!.posVec.squareDistanceTo(point) <= rangeSq }
	}
	
	// Reified
	
	inline fun <reified T : Entity> inDimension() = inBox(T::class.java, INFINITE_AABB)
	inline fun <reified T : Entity> inBox(aabb: AxisAlignedBB) = inBox(T::class.java, aabb)
	inline fun <reified T : Entity> inRange(point: Vec3d, range: Double) = inRange(T::class.java, point, range)
	
	// General
	
	inline fun allInDimension() = inBox(Entity::class.java, INFINITE_AABB)
	inline fun allInBox(aabb: AxisAlignedBB) = inBox(Entity::class.java, aabb)
	inline fun allInRange(point: Vec3d, range: Double) = inRange(Entity::class.java, point, range)
}
