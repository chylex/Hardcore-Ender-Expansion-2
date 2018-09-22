package chylex.hee.game.entity.util
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import com.google.common.base.Predicate
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EntitySelector(val world: World, val predicate: Predicate<in Entity>){
	inline fun <reified T : Entity> inBox(aabb: AxisAlignedBB): Sequence<T>{
		return world.getEntitiesWithinAABB(T::class.java, aabb, predicate).asSequence()
	}
	
	inline fun <reified T : Entity> inRange(point: Vec3d, range: Double): Sequence<T>{
		val aabb = AxisAlignedBB(point.x - range, point.y - range, point.z - range, point.x + range, point.y + range, point.z + range)
		val rangeSquared = square(range)
		
		return inBox<T>(aabb).filter { it.posVec.squareDistanceTo(point) <= rangeSquared }
	}
	
	fun allInBox(aabb: AxisAlignedBB) = inBox<Entity>(aabb)
	fun allInRange(point: Vec3d, range: Double) = inRange<Entity>(point, range)
}
