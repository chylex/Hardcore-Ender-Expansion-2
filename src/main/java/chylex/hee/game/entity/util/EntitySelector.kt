package chylex.hee.game.entity.util
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import com.google.common.base.Predicate
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

@Suppress("NOTHING_TO_INLINE")
class EntitySelector(private val world: World, private val predicate: Predicate<in Entity>){
	fun <T : Entity> inBox(cls: Class<T>, aabb: AxisAlignedBB): List<T>{
		return world.getEntitiesWithinAABB(cls, aabb, predicate)
	}
	
	fun <T : Entity> inRange(cls: Class<T>, point: Vec3d, range: Double): List<T>{
		val aabb = AxisAlignedBB(point.x - range, point.y - range, point.z - range, point.x + range, point.y + range, point.z + range)
		val rangeSq = square(range)
		
		return world.getEntitiesWithinAABB(cls, aabb){ predicate.apply(it) && it!!.posVec.squareDistanceTo(point) <= rangeSq }
	}
	
	// Reified
	
	inline fun <reified T : Entity> inBox(aabb: AxisAlignedBB) = inBox(T::class.java, aabb)
	inline fun <reified T : Entity> inRange(point: Vec3d, range: Double) = inRange(T::class.java, point, range)
	
	// General
	
	inline fun allInBox(aabb: AxisAlignedBB) = inBox(Entity::class.java, aabb)
	inline fun allInRange(point: Vec3d, range: Double) = inRange(Entity::class.java, point, range)
}
