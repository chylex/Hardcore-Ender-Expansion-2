package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.IBlockStateModel
import chylex.hee.game.block.util.Property
import chylex.hee.game.block.util.with
import chylex.hee.game.world.util.getBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

sealed class BlockSimpleMergingBottom(builder: BlockBuilder, private val mergeBottom: Block) : HeeBlock(builder) {
	companion object {
		val MERGE = Property.bool("merge")
	}
	
	abstract override val model: IBlockStateModel
	
	init {
		defaultState = stateContainer.baseState.with(MERGE, false)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(MERGE)
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		val world = context.world
		val pos = context.pos
		
		return this.with(MERGE, pos.down().getBlock(world) === mergeBottom)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
		return when (facing) {
			DOWN -> state.with(MERGE, pos.down().getBlock(world) === mergeBottom)
			else -> state
		}
	}
}
