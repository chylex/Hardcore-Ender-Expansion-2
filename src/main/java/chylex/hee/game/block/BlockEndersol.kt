package chylex.hee.game.block
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.getBlock
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockEndersol(builder: BlockSimple.Builder) : BlockSimple(builder){
	private companion object{
		private val MERGE_TOP = PropertyBool.create("merge_top")
		private val MERGE_BOTTOM = PropertyBool.create("merge_bottom")
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, MERGE_TOP, MERGE_BOTTOM)
	
	override fun getMetaFromState(state: IBlockState): Int = 0
	
	override fun getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState{
		return state.withProperty(MERGE_TOP, pos.up().getBlock(world) === ModBlocks.HUMUS)
		            .withProperty(MERGE_BOTTOM, pos.down().getBlock(world) === Blocks.END_STONE)
	}
}
