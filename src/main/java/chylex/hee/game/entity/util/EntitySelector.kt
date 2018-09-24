package chylex.hee.game.entity.util
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import com.google.common.base.Predicate
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EntitySelector(private val world: World, private val predicate: Predicate<in Entity>){
	fun <T : Entity> inBox(cls: Class<T>, aabb: AxisAlignedBB): Sequence<T>{
		return world.getEntitiesWithinAABB(cls, aabb, predicate).asSequence()
	}
	
	fun <T : Entity> inRange(cls: Class<T>, point: Vec3d, range: Double): Sequence<T>{
		val aabb = AxisAlignedBB(point.x - range, point.y - range, point.z - range, point.x + range, point.y + range, point.z + range)
		val rangeSquared = square(range)
		
		return inBox(cls, aabb).filter { it.posVec.squareDistanceTo(point) <= rangeSquared }
	}
	
	// Reified
	
	inline fun <reified T : Entity> inBox(aabb: AxisAlignedBB): Sequence<T> = inBox(T::class.java, aabb)
	inline fun <reified T : Entity> inRange(point: Vec3d, range: Double): Sequence<T> = inRange(T::class.java, point, range)
	
	// General
	
	inline fun allInBox(aabb: AxisAlignedBB) = inBox(Entity::class.java, aabb)
	inline fun allInRange(point: Vec3d, range: Double) = inRange(Entity::class.java, point, range)
}
