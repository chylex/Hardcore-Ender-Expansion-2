package chylex.hee.game.world.generation.structure.world.segments

import chylex.hee.util.math.Size
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class SegmentSingleState(private val size: Size, private val fillState: BlockState) : ISegment {
	constructor(size: Size, block: Block) : this(size, block.defaultState)
	
	override fun getState(pos: BlockPos): BlockState {
		return fillState
	}
	
	override fun withState(pos: BlockPos, state: BlockState): ISegment {
		return if (state == fillState)
			this
		else
			SegmentFull(size, fillState).withState(pos, state)
	}
}
