package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.Blocks
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class BlockEndersol(builder: BlockBuilder) : BlockSimple(builder){
	private companion object{
		private val MERGE_TOP = Property.bool("merge_top")
		private val MERGE_BOTTOM = Property.bool("merge_bottom")
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(MERGE_TOP, MERGE_BOTTOM)
	}
	
	// UPDATE placement state
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
		return when(facing){
			UP   -> state.with(MERGE_TOP, neighborState.block === ModBlocks.HUMUS)
			DOWN -> state.with(MERGE_BOTTOM, neighborState.block === Blocks.END_STONE)
			else -> state
		}
	}
}
