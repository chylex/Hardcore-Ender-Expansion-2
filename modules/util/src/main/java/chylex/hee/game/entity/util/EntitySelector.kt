package chylex.hee.game.entity.util

import chylex.hee.util.math.square
import com.google.common.base.Predicates
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.util.EntityPredicates
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.IEntityReader
import net.minecraft.world.IWorld
import net.minecraft.world.server.ServerWorld
import java.util.function.Predicate

@Suppress("NOTHING_TO_INLINE")
class EntitySelector(private val world: IEntityReader, private val predicate: Predicate<in Entity>) {
	fun <T : Entity> inBox(cls: Class<T>, aabb: AxisAlignedBB): List<T> {
		return world.getEntitiesWithinAABB(cls, aabb, predicate)
	}
	
	fun <T : Entity> inRange(cls: Class<T>, point: Vector3d, range: Double): List<T> {
		val aabb = AxisAlignedBB(point.x - range, point.y - range, point.z - range, point.x + range, point.y + range, point.z + range)
		val rangeSq = square(range)
		
		return world.getEntitiesWithinAABB(cls, aabb) { predicate.test(it) && it!!.posVec.squareDistanceTo(point) <= rangeSq }
	}
	
	// Reified
	
	inline fun <reified T : Entity> inBox(aabb: AxisAlignedBB) = inBox(T::class.java, aabb)
	inline fun <reified T : Entity> inRange(point: Vector3d, range: Double) = inRange(T::class.java, point, range)
	
	// General
	
	inline fun allInBox(aabb: AxisAlignedBB) = inBox(Entity::class.java, aabb)
	inline fun allInRange(point: Vector3d, range: Double) = inRange(Entity::class.java, point, range)
}

private val predicateNotSpectating = EntityPredicates.NOT_SPECTATING
private val predicateAliveAndNotSpectating = EntityPredicates.IS_ALIVE.and(predicateNotSpectating)
private val predicateAliveAndTargetable = EntityPredicates.IS_ALIVE.and(EntityPredicates.CAN_AI_TARGET)
private val predicateAlwaysTrue = Predicates.alwaysTrue<Entity>()

/**
 * Selects all entities in the dimension.
 */
val IWorld.selectAllEntities: Iterable<Entity>
	get() = when (this) {
		is ServerWorld -> this.entitiesIteratable
		is ClientWorld -> this.allEntities
		else           -> emptyList()
	}

/**
 * Selects all entities which are not spectators.
 */
val IEntityReader.selectEntities
	get() = EntitySelector(this, predicateNotSpectating)

/**
 * Selects all entities which have not been removed from the world, and are not spectators.
 */
val IEntityReader.selectExistingEntities
	get() = EntitySelector(this, predicateAliveAndNotSpectating)

/**
 * Selects all entities which have not been removed from the world, and are not spectators or creative mode players.
 */
val IEntityReader.selectVulnerableEntities
	get() = EntitySelector(this, predicateAliveAndTargetable)

/**
 * Selects all entities and spectators.
 */
val IEntityReader.selectEntitiesAndSpectators
	get() = EntitySelector(this, predicateAlwaysTrue)
