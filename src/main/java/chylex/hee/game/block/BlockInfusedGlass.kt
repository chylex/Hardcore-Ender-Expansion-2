package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockRenderLayer.TRANSLUCENT
import chylex.hee.game.block.util.Property
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.getBlock
import net.minecraft.block.AbstractGlassBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class BlockInfusedGlass(builder: BlockBuilder) : AbstractGlassBlock(builder.p), IHeeBlock {
	companion object {
		val CONNECT_DOWN = Property.bool("connect_d")
		val CONNECT_UP = Property.bool("connect_u")
		val CONNECT_NORTH = Property.bool("connect_n")
		val CONNECT_SOUTH = Property.bool("connect_s")
		val CONNECT_EAST = Property.bool("connect_e")
		val CONNECT_WEST = Property.bool("connect_w")
		
		private val CONNECT_MAPPINGS = mapOf(
			DOWN to CONNECT_DOWN,
			UP to CONNECT_UP,
			NORTH to CONNECT_NORTH,
			SOUTH to CONNECT_SOUTH,
			EAST to CONNECT_EAST,
			WEST to CONNECT_WEST
		)
	}
	
	override val renderLayer
		get() = TRANSLUCENT
	
	init {
		defaultState = Facing6.fold(stateContainer.baseState) { acc, facing -> acc.with(CONNECT_MAPPINGS.getValue(facing), false) }
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(CONNECT_DOWN, CONNECT_UP, CONNECT_NORTH, CONNECT_SOUTH, CONNECT_EAST, CONNECT_WEST)
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		val world = context.world
		val pos = context.pos
		
		return Facing6.fold(defaultState) { acc, facing -> acc.with(CONNECT_MAPPINGS.getValue(facing), pos.offset(facing).getBlock(world) === this) } // POLISH improve corners
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
		return state.with(CONNECT_MAPPINGS.getValue(facing), pos.offset(facing).getBlock(world) === this)
	}
}
