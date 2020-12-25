package chylex.hee.game.world.generation.segments

import chylex.hee.game.world.generation.ISegment
import chylex.hee.game.world.generation.ISegment.Companion.index
import chylex.hee.game.world.math.Size
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class SegmentFull(private val size: Size, fillState: BlockState) : ISegment {
	private val data = Array(size.x * size.y * size.z) { fillState }
	
	override fun getState(pos: BlockPos): BlockState {
		return data[index(pos, size)]
	}
	
	override fun withState(pos: BlockPos, state: BlockState): ISegment {
		data[index(pos, size)] = state
		return this
	}
}
