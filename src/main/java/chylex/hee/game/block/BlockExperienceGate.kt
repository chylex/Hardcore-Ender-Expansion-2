package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.game.block.logic.IFullBlockCollisionHandler
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.offsetWhile
import chylex.hee.game.world.util.setBlock
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.ExperienceOrbEntity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

abstract class BlockExperienceGate(builder: BlockBuilder) : BlockSimple(builder), IFullBlockCollisionHandler {
	protected open fun findController(world: IBlockReader, pos: BlockPos): TileEntityExperienceGate? {
		for (offset in pos.allInCenteredBox(1, 0, 1)) {
			val tile = offset.getTile<TileEntityExperienceGate>(world)
			
			if (tile != null) {
				return tile
			}
		}
		
		return null
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		if (world.isAreaLoaded(pos, 3)) {
			val nw = pos.offsetWhile(NORTH, 1..3) { it.getBlock(world) === this }.offsetWhile(WEST, 1..3) { it.getBlock(world) === this }
			val se = pos.offsetWhile(SOUTH, 1..3) { it.getBlock(world) === this }.offsetWhile(EAST, 1..3) { it.getBlock(world) === this }
			
			if (se.x - nw.x == 2 && se.z - nw.z == 2) {
				val center = Pos((se.x + nw.x) / 2, pos.y, (se.z + nw.z) / 2)
				
				if (center.getBlock(world) === this) {
					center.setBlock(world, ModBlocks.EXPERIENCE_GATE_CONTROLLER)
				}
			}
		}
	}
	
	override fun onEntityCollisionAbove(world: World, pos: BlockPos, entity: Entity) {
		if (!world.isRemote && entity.ticksExisted > 10 && entity.isAlive) {
			when (entity) {
				is PlayerEntity        -> findController(world, pos)?.onCollision(entity)
				is ItemEntity          -> findController(world, pos)?.onCollision(entity)
				is ExperienceOrbEntity -> findController(world, pos)?.onCollision(entity)
			}
		}
	}
}
