package chylex.hee.game.block
import chylex.hee.game.block.util.Property
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.with
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockEndersol(builder: BlockSimple.Builder) : BlockSimple(builder){
	private companion object{
		private val MERGE_TOP = Property.bool("merge_top")
		private val MERGE_BOTTOM = Property.bool("merge_bottom")
	}
	
	override fun createBlockState() = BlockStateContainer(this, MERGE_TOP, MERGE_BOTTOM)
	
	override fun getMetaFromState(state: IBlockState) = 0
	
	override fun getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState{
		return state.with(MERGE_TOP, pos.up().getBlock(world) === ModBlocks.HUMUS)
		            .with(MERGE_BOTTOM, pos.down().getBlock(world) === Blocks.END_STONE)
	}
}
