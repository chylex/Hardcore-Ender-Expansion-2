package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockAddedComponent
import chylex.hee.game.block.components.IBlockEntityComponent
import chylex.hee.game.block.components.IBlockPlacementComponent
import chylex.hee.game.block.components.IBlockShapeComponent
import chylex.hee.game.block.components.IPlayerUseBlockComponent
import chylex.hee.game.block.components.ISetBlockStateFromNeighbor
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.block.logic.IBlockDynamicHardness
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.IBlockStateModelSupplier
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.block.util.FURNACE_FACING
import chylex.hee.game.block.util.Property
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.getTile
import net.minecraft.block.BlockRenderType.ENTITYBLOCK_ANIMATED
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.tileentity.FurnaceTileEntity
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
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

object BlockIgneousPlate : HeeBlockBuilder() {
	val FACING_NOT_DOWN = Property.facing("facing", Facing6.minusElement(DOWN))
	
	private const val BB_SIDE_MIN = 0.125
	private const val BB_SIDE_MAX = 0.875
	private const val BB_TOP = 0.125
	
	private val BOUNDING_BOX = mapOf(
		UP    to VoxelShapes.create(BB_SIDE_MIN, 0.0, BB_SIDE_MIN, BB_SIDE_MAX, BB_TOP, BB_SIDE_MAX),
		NORTH to VoxelShapes.create(BB_SIDE_MIN, BB_SIDE_MIN, 1.0 - BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX, 1.0),
		SOUTH to VoxelShapes.create(BB_SIDE_MIN, BB_SIDE_MIN, 0.0, BB_SIDE_MAX, BB_SIDE_MAX, BB_TOP),
		EAST  to VoxelShapes.create(0.0, BB_SIDE_MIN, BB_SIDE_MIN, BB_TOP, BB_SIDE_MAX, BB_SIDE_MAX),
		WEST  to VoxelShapes.create(1.0 - BB_TOP, BB_SIDE_MIN, BB_SIDE_MIN, 1.0, BB_SIDE_MAX, BB_SIDE_MAX)
	)
	
	init {
		model = IBlockStateModelSupplier {
			BlockStateModel(BlockStatePreset.Simple, BlockModel.ParticleOnly(it.location), ItemModel.Simple)
		}
		
		material = Materials.IGNEOUS_ROCK_PLATE
		color = MaterialColor.AIR
		sound = SoundType.STONE
		
		components.states.set(FACING_NOT_DOWN, default = UP)
		components.states.facingProperty = FACING_NOT_DOWN
		
		components.shape = object : IBlockShapeComponent {
			override fun getShape(state: BlockState): VoxelShape {
				return BOUNDING_BOX[state[FACING_NOT_DOWN]] ?: BOUNDING_BOX.getValue(UP)
			}
		}
		
		components.renderType = ENTITYBLOCK_ANIMATED
		
		components.entity = IBlockEntityComponent(::TileEntityIgneousPlate)
		
		components.placement = object : IBlockPlacementComponent {
			override fun isPositionValid(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean {
				return FACING_NOT_DOWN.allowedValues.any { canPlacePlateAt(world, pos, it) }
			}
			
			override fun getPlacedState(defaultState: BlockState, world: World, pos: BlockPos, context: BlockItemUseContext): BlockState {
				return if (canPlacePlateAt(world, pos, context.face))
					defaultState.with(FACING_NOT_DOWN, context.face)
				else
					FACING_NOT_DOWN.allowedValues.firstOrNull { canPlacePlateAt(world, pos, it) }?.let { defaultState.with(FACING_NOT_DOWN, it) } ?: defaultState
			}
		}
		
		components.onAdded = IBlockAddedComponent { state, world, pos ->
			pos.offset(state[FACING_NOT_DOWN].opposite).getTile<FurnaceTileEntity>(world)?.let(EntityTechnicalIgneousPlateLogic.Companion::createForFurnace)
		}
		
		components.setStateFromNeighbor = ISetBlockStateFromNeighbor { state, world, pos, _, _ ->
			if (!canPlacePlateAt(world, pos, state[FACING_NOT_DOWN]))
				Blocks.AIR.defaultState
			else
				state
		}
		
		components.playerUse = object : IPlayerUseBlockComponent {
			override fun use(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand): ActionResultType {
				val heldItem = player.getHeldItem(hand)
				if (heldItem.item !== Items.WATER_BUCKET) {
					return PASS
				}
				
				if (!world.isRemote && tryCoolPlate(world, pos, state) && !player.abilities.isCreativeMode) {
					player.setHeldItem(hand, ItemStack(Items.BUCKET))
				}
				
				return SUCCESS
			}
		}
		
		interfaces[IBlockDynamicHardness::class.java] = object : IBlockDynamicHardness {
			override fun getBlockHardness(world: IBlockReader, pos: BlockPos, state: BlockState, originalHardness: Float): Float {
				val tile = pos.getTile<TileEntityIgneousPlate>(world) ?: return 0F
				
				return when {
					tile.isOverheating -> 10F
					tile.isWorking     -> 4F
					else               -> 0F
				}
			}
		}
	}
	
	fun tryCoolPlate(world: World, pos: BlockPos, state: BlockState): Boolean {
		return pos.offset(state[FACING_NOT_DOWN].opposite).getTile<FurnaceTileEntity>(world)?.let(EntityTechnicalIgneousPlateLogic.Companion::triggerCooling) == true
	}
	
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
