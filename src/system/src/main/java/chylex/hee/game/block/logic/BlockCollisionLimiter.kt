package chylex.hee.game.block.logic

import chylex.hee.game.world.totalTime
import net.minecraft.entity.Entity
import net.minecraft.world.World
import java.util.UUID

class BlockCollisionLimiter {
	private var lastCollidingEntity = ThreadLocal<Pair<Long, UUID>?>()
	
	/**
	 * Prevents handling collision for an entity multiple times if the entity is touching 2 or more blocks.
	 *
	 * Because onEntityCollision is always called in succession for all blocks colliding with an entity,
	 * it is enough to compare if either the world time or the entity has changed since last call (on the same thread).
	 *
	 * Returns true if the collision should be handled.
	 */
	fun check(world: World, entity: Entity): Boolean {
		val currentWorldTime = world.totalTime
		
		if (lastCollidingEntity.get()?.takeUnless { it.first != currentWorldTime || it.second != entity.uniqueID } == null) {
			lastCollidingEntity.set(Pair(currentWorldTime, entity.uniqueID))
			return true
		}
		
		return false
	}
}
