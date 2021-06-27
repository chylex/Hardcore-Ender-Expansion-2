package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.allInBox
import chylex.hee.game.world.allInBoxMutable
import chylex.hee.game.world.distanceTo
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.isAir
import chylex.hee.game.world.isTopSolid
import chylex.hee.game.world.max
import chylex.hee.game.world.min
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.setAir
import chylex.hee.game.world.setBlock
import chylex.hee.system.facades.Facing4
import chylex.hee.system.math.LerpedFloat
import chylex.hee.system.math.floorToInt
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType.INVISIBLE
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

abstract class BlockAbstractPortal(builder: BlockBuilder) : BlockSimpleShaped(builder, AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75, 1.0)) {
	companion object {
		const val MAX_DISTANCE_FROM_FRAME = 6.0
		const val MAX_SIZE = 5
		
		const val TRANSLATION_SPEED_LONG = 600000L
		const val TRANSLATION_SPEED_INV = 1.0 / TRANSLATION_SPEED_LONG
		
		private val COLLISION_AABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.025, 1.0).asVoxelShape
		
		fun findInnerArea(world: World, controllerPos: BlockPos, frameBlock: Block): Pair<BlockPos, BlockPos>? {
			val mirrorRange = 1..(MAX_SIZE + 1)
			val halfRange = 1..(1 + (MAX_SIZE / 2))
			
			for(facing in Facing4) {
				val mirrorPos = controllerPos.offsetUntil(facing, mirrorRange) { it.getBlock(world) === frameBlock } ?: continue
				val centerPos = controllerPos.offset(facing, controllerPos.distanceTo(mirrorPos).floorToInt() / 2)
				
				val perpendicular1 = centerPos.offsetUntil(facing.rotateY(), halfRange) { it.getBlock(world) === frameBlock } ?: continue
				val perpendicular2 = centerPos.offsetUntil(facing.rotateYCCW(), halfRange) { it.getBlock(world) === frameBlock } ?: continue
				
				val minPos = controllerPos.min(mirrorPos).min(perpendicular1).min(perpendicular2).add(1, 0, 1)
				val maxPos = controllerPos.max(mirrorPos).max(perpendicular1).max(perpendicular2).add(-1, 0, -1)
				
				if (maxPos.x - minPos.x != maxPos.z - minPos.z) {
					return null
				}
				
				return minPos to maxPos
			}
			
			return null
		}
		
		fun spawnInnerBlocks(world: World, controllerPos: BlockPos, frameBlock: Block, innerBlock: Block, minSize: Int) {
			val (minPos, maxPos) = findInnerArea(world, controllerPos, frameBlock) ?: return
			
			if (maxPos.x - minPos.x + 1 >= minSize &&
			    maxPos.z - minPos.z + 1 >= minSize &&
			    minPos.allInBoxMutable(maxPos).all { it.isAir(world) }
			) {
				minPos.allInBoxMutable(maxPos).forEach { it.setBlock(world, innerBlock) }
			}
		}
		
		fun ensureClearance(world: World, spawnPos: BlockPos, radius: Int) {
			for(pos in spawnPos.add(-radius, 1, -radius).allInBox(spawnPos.add(radius, 2, radius))) {
				pos.setAir(world)
			}
		}
		
		fun ensurePlatform(world: World, spawnPos: BlockPos, block: Block, radius: Int) {
			for(pos in spawnPos.add(-radius, -1, -radius).allInBox(spawnPos.add(radius, -1, radius))) {
				if (!pos.isTopSolid(world)) {
					pos.setBlock(world, block)
				}
			}
		}
	}
	
	interface IPortalController {
		val clientAnimationProgress: LerpedFloat
		val clientPortalOffset: LerpedFloat
	}
	
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	abstract override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity
	protected abstract fun onEntityInside(world: World, pos: BlockPos, entity: Entity)
	
	final override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		if (!world.isRemote && !entity.isPassenger && !entity.isBeingRidden && entity.canChangeDimension() && entity.posY <= pos.y + 0.05) {
			onEntityInside(world, pos, entity)
		}
	}
	
	override fun getCollisionShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return COLLISION_AABB
	}
	
	final override fun getRenderType(state: BlockState) = INVISIBLE
}
