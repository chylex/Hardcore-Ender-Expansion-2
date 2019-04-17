package chylex.hee.game.world.generation.segments
import chylex.hee.game.world.generation.ISegment
import chylex.hee.game.world.util.Size
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

class SegmentSingleState(private val size: Size, private val fillState: IBlockState) : ISegment{
	constructor(size: Size, block: Block) : this(size, block.defaultState)
	
	override fun getState(pos: BlockPos): IBlockState{
		return fillState
	}
	
	override fun withState(pos: BlockPos, state: IBlockState): ISegment{
		return if (state != fillState)
			SegmentFull(size, fillState).withState(pos, state)
		else
			this
	}
}
