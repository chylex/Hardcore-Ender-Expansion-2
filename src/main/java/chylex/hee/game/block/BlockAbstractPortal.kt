package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockCollideWithEntityComponent
import chylex.hee.game.block.components.IBlockEntityComponent
import chylex.hee.game.block.components.IBlockShapeComponent
import chylex.hee.game.block.interfaces.IBlockInterface
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.entity.util.EntityPortalContact
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.allInBox
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.game.world.util.distanceTo
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.isTopSolid
import chylex.hee.game.world.util.max
import chylex.hee.game.world.util.min
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.setAir
import chylex.hee.game.world.util.setBlock
import chylex.hee.util.math.LerpedFloat
import chylex.hee.util.math.floorToInt
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType.INVISIBLE
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.Entity
import net.minecraft.tags.BlockTags
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.World

class BlockAbstractPortal(impl: IInnerPortalBlock) : HeeBlockBuilder() {
	companion object {
		const val MAX_DISTANCE_FROM_FRAME = 6.0
		const val MAX_SIZE = 5
		
		private const val TRANSLATION_SPEED_LONG = 600000L
		const val TRANSLATION_SPEED_INV = 1.0 / TRANSLATION_SPEED_LONG
		
		private val SHAPE = VoxelShapes.create(0.0, 0.0, 0.0, 1.0, 0.75, 1.0)
		private val COLLISION_SHAPE = VoxelShapes.create(0.0, 0.0, 0.0, 1.0, 0.025, 1.0)
		
		fun findInnerArea(world: World, controllerPos: BlockPos, frameBlock: Block): Pair<BlockPos, BlockPos>? {
			val mirrorRange = 1..(MAX_SIZE + 1)
			val halfRange = 1..(1 + (MAX_SIZE / 2))
			
			for (facing in Facing4) {
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
			for (pos in spawnPos.add(-radius, 1, -radius).allInBox(spawnPos.add(radius, 2, radius))) {
				pos.setAir(world)
			}
		}
		
		fun ensurePlatform(world: World, spawnPos: BlockPos, block: Block, radius: Int) {
			for (pos in spawnPos.add(-radius, -1, -radius).allInBox(spawnPos.add(radius, -1, radius))) {
				if (!pos.isTopSolid(world)) {
					pos.setBlock(world, block)
				}
			}
		}
	}
	
	init {
		includeFrom(BlockIndestructible)
		
		localization = LocalizationStrategy.DeleteWords("Inner")
		
		model = BlockStateModel(BlockStatePreset.SimpleFrom(Blocks.END_PORTAL), BlockModel.Manual)
		
		material = Material.PORTAL
		color = MaterialColor.BLACK
		sound = SoundType.STONE
		light = 15
		
		isSolid = false
		
		tags.add(BlockTags.PORTALS)
		
		components.shape = object : IBlockShapeComponent {
			override fun getShape(state: BlockState): VoxelShape {
				return SHAPE
			}
			
			override fun getCollisionShape(state: BlockState): VoxelShape? {
				return COLLISION_SHAPE
			}
		}
		
		components.renderType = INVISIBLE
		
		components.entity = IBlockEntityComponent(impl::createTileEntity)
		
		components.collideWithEntity = IBlockCollideWithEntityComponent { _, world, pos, entity ->
			if (!world.isRemote && !entity.isPassenger && !entity.isBeingRidden && entity.canChangeDimension() && entity.posY <= pos.y + 0.05 && EntityPortalContact.shouldTeleport(entity)) {
				impl.teleportEntity(world, pos, entity)
			}
		}
		
		interfaces[IInnerPortalBlock::class.java] = impl
	}
	
	interface IInnerPortalBlock : IBlockInterface {
		fun createTileEntity(): TileEntity
		fun teleportEntity(world: World, pos: BlockPos, entity: Entity)
	}
	
	interface IPortalController {
		val clientAnimationProgress: LerpedFloat
		val clientPortalOffset: LerpedFloat
	}
}
