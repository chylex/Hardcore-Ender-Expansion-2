package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class BlockEndersol(builder: BlockBuilder) : BlockSimple(builder){
	companion object{
		val MERGE_TOP = Property.bool("merge_top")
		val MERGE_BOTTOM = Property.bool("merge_bottom")
	}
	
	init{
		defaultState = stateContainer.baseState.with(MERGE_TOP, false).with(MERGE_BOTTOM, false)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(MERGE_TOP, MERGE_BOTTOM)
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState{
		val world = context.world
		val pos = context.pos
		
		return this.with(MERGE_TOP, pos.up().getBlock(world) === ModBlocks.HUMUS)
		           .with(MERGE_BOTTOM, pos.down().getBlock(world) === Blocks.END_STONE)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
		return when(facing){
			UP   -> state.with(MERGE_TOP, pos.up().getBlock(world) === ModBlocks.HUMUS)
			DOWN -> state.with(MERGE_BOTTOM, pos.down().getBlock(world) === Blocks.END_STONE)
			else -> state
		}
	}
}
