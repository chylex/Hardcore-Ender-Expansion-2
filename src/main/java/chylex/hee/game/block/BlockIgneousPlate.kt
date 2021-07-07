package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.block.logic.IBlockDynamicHardness
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.util.FURNACE_FACING
import chylex.hee.game.block.util.Property
import chylex.hee.game.block.util.asVoxelShape
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.getTile
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType.ENTITYBLOCK_ANIMATED
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.FurnaceTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import net.minecraft.util.Hand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

class BlockIgneousPlate(builder: BlockBuilder) : BlockSimple(builder), IBlockDynamicHardness {
	companion object {
		val FACING_NOT_DOWN = Property.facing("facing", Facing6.minusElement(DOWN))
		
		private const val BB_SIDE_MIN = 0.125
		private const val BB_SIDE_MAX = 0.875
		private const val BB_TOP = 0.125
		
		private val BOUNDING_BOX = mapOf(
			UP    to AxisAlignedBB(BB_SIDE_MIN, 0.0, BB_SIDE_MIN, BB_SIDE_MAX, BB_TOP, BB_SIDE_MAX).asVoxelShape,
			NORTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 1.0 - BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX, 1.0).asVoxelShape,
			SOUTH to AxisAlignedBB(BB_SIDE_MIN, BB_SIDE_MIN, 0.0, BB_SIDE_MAX, BB_SIDE_MAX, BB_TOP).asVoxelShape,
			EAST  to AxisAlignedBB(0.0, BB_SIDE_MIN, BB_SIDE_MIN, BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX).asVoxelShape,
			WEST  to AxisAlignedBB(1.0 - BB_TOP, BB_SIDE_MIN, BB_SIDE_MIN, 1.0, BB_SIDE_MAX, BB_SIDE_MAX).asVoxelShape
		)
		
		private fun canPlacePlateAt(world: IWorldReader, pos: BlockPos, facing: Direction): Boolean {
			val furnacePos = pos.offset(facing.opposite)
			val state = furnacePos.getState(world)
			
			return (
				state.properties.contains(FURNACE_FACING) &&
				state[FURNACE_FACING] != facing &&
				furnacePos.getTile<FurnaceTileEntity>(world) != null
			)
		}
	}
	
	init {
		defaultState = stateContainer.baseState.with(FACING_NOT_DOWN, UP)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(FACING_NOT_DOWN)
	}
	
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityIgneousPlate()
	}
	
	// Placement rules
	
	override fun isValidPosition(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean {
		return FACING_NOT_DOWN.allowedValues.any { canPlacePlateAt(world, pos, it) }
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		val world = context.world
		val pos = context.pos
		val facing = context.face
		
		return if (canPlacePlateAt(world, pos, facing))
			defaultState.with(FACING_NOT_DOWN, facing)
		else
			FACING_NOT_DOWN.allowedValues.firstOrNull { canPlacePlateAt(world, pos, it) }?.let { defaultState.with(FACING_NOT_DOWN, it) } ?: defaultState
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		pos.offset(state[FACING_NOT_DOWN].opposite).getTile<FurnaceTileEntity>(world)?.let(EntityTechnicalIgneousPlateLogic.Companion::createForFurnace)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
		@Suppress("DEPRECATION")
		return if (!canPlacePlateAt(world, pos, state[FACING_NOT_DOWN]))
			Blocks.AIR.defaultState
		else
			super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
	}
	
	// Interactions
	
	override fun getBlockHardness(world: IBlockReader, pos: BlockPos, state: BlockState, originalHardness: Float): Float {
		val tile = pos.getTile<TileEntityIgneousPlate>(world) ?: return 0F
		
		return when {
			tile.isOverheating -> 10F
			tile.isWorking     -> 4F
			else               -> 0F
		}
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.item === Items.WATER_BUCKET) {
			if (!world.isRemote && tryCoolPlate(world, pos, state) && !player.abilities.isCreativeMode) {
				player.setHeldItem(hand, ItemStack(Items.BUCKET))
			}
			
			return SUCCESS
		}
		
		return PASS
	}
	
	fun tryCoolPlate(world: World, pos: BlockPos, state: BlockState): Boolean {
		return pos.offset(state[FACING_NOT_DOWN].opposite).getTile<FurnaceTileEntity>(world)?.let(EntityTechnicalIgneousPlateLogic.Companion::triggerCooling) == true
	}
	
	// State handling
	
	override fun rotate(state: BlockState, rot: Rotation): BlockState {
		return state.with(FACING_NOT_DOWN, rot.rotate(state[FACING_NOT_DOWN]))
	}
	
	override fun mirror(state: BlockState, mirror: Mirror): BlockState {
		return state.with(FACING_NOT_DOWN, mirror.mirror(state[FACING_NOT_DOWN]))
	}
	
	// Rendering
	
	override fun getShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return BOUNDING_BOX[state[FACING_NOT_DOWN]] ?: BOUNDING_BOX.getValue(UP)
	}
	
	override fun getRenderType(state: BlockState) = ENTITYBLOCK_ANIMATED
}
