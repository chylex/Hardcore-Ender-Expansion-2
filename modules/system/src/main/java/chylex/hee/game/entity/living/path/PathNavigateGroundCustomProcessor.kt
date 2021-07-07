package chylex.hee.game.entity.living.path

import net.minecraft.entity.MobEntity
import net.minecraft.pathfinding.GroundPathNavigator
import net.minecraft.pathfinding.NodeProcessor
import net.minecraft.pathfinding.PathFinder
import net.minecraft.world.World

abstract class PathNavigateGroundCustomProcessor(entity: MobEntity, world: World) : GroundPathNavigator(entity, world) {
	override fun getPathFinder(followRange: Int): PathFinder {
		nodeProcessor = createNodeProcessor()
		return PathFinder(nodeProcessor, followRange)
	}
	
	protected abstract fun createNodeProcessor(): NodeProcessor
}
