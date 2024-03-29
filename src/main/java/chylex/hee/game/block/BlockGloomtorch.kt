package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.block.util.asVoxelShape
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.world.util.Facing6
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.DirectionalBlock
import net.minecraft.block.HorizontalFaceBlock
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.IWorldReader

class BlockGloomtorch(builder: BlockBuilder) : DirectionalBlock(builder.p), IHeeBlock {
	private companion object {
		private const val BB_SIDE_MIN = 0.421875
		private const val BB_SIDE_MAX = 0.578125
		private const val BB_TOP = 0.59375
		
		private val BOUNDING_BOX = mapOf(
			UP    to AxisAlignedBB(BB_SIDE_MIN, 0.0, BB_SIDE_MIN, BB_SIDE_MAX, BB_TOP, BB_SIDE_MAX).asVoxelShape,
			DOWN  to AxisAlignedBB(BB_SIDE_MIN, 1.0 - BB_TOP, BB_SIDE_MIN, BB_SIDE_MAX, 1.0, BB_SIDE_MAX).asVoxelShape,
			NORTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 1.0 - BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX, 1.0).asVoxelShape,
			SOUTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 0.0, BB_SIDE_MAX, BB_SIDE_MAX, BB_TOP).asVoxelShape,
			EAST  to AxisAlignedBB(0.0, BB_SIDE_MIN, BB_SIDE_MIN, BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX).asVoxelShape,
			WEST  to AxisAlignedBB(1.0 - BB_TOP, BB_SIDE_MIN, BB_SIDE_MIN, 1.0, BB_SIDE_MAX, BB_SIDE_MAX).asVoxelShape
		)
		
		private fun canPlaceGloomtorchAt(world: IWorldReader, pos: BlockPos, facing: Direction): Boolean {
			return HorizontalFaceBlock.isSideSolidForDirection(world, pos, facing.opposite)
		}
	}
	
	override val model
		get() = BlockStateModels.ItemOnly(ItemModel.Simple, asItem = true)
	
	override val renderLayer
		get() = CUTOUT
	
	init {
		defaultState = stateContainer.baseState.withFacing(UP)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(FACING)
	}
	
	// Placement rules
	
	override fun isValidPosition(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean {
		return Facing6.any { canPlaceGloomtorchAt(world, pos, it) }
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		val world = context.world
		val pos = context.pos
		val facing = context.face
		
		return if (canPlaceGloomtorchAt(world, pos, facing))
			this.withFacing(facing)
		else
			Facing6.firstOrNull { canPlaceGloomtorchAt(world, pos, it) }?.let(this::withFacing) ?: defaultState
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
		@Suppress("DEPRECATION")
		return if (facing.opposite == state[FACING] && !canPlaceGloomtorchAt(world, pos, state[FACING]))
			Blocks.AIR.defaultState
		else
			super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
	}
	
	// State handling
	
	override fun rotate(state: BlockState, rot: Rotation): BlockState {
		return state.withFacing(rot.rotate(state[FACING]))
	}
	
	override fun mirror(state: BlockState, mirror: Mirror): BlockState {
		return state.withFacing(mirror.mirror(state[FACING]))
	}
	
	// Shape and rendering
	
	override fun getShape(state: BlockState, source: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return BOUNDING_BOX[state[FACING]] ?: BOUNDING_BOX.getValue(UP)
	}
}
