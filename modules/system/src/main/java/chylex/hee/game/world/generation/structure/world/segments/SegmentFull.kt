package chylex.hee.game.world.generation.structure.world.segments

import chylex.hee.game.world.generation.structure.world.segments.ISegment.Companion.index
import chylex.hee.util.math.MutablePos
import chylex.hee.util.math.Size
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

/**
 * A segment that supports arbitrary block states in arbitrary positions.
 */
class SegmentFull(private val size: Size, fillState: BlockState) : ISegment {
	constructor(size: Size, fillState: BlockState, posToStateMap: Long2ObjectOpenHashMap<BlockState>) : this(size, fillState) {
		val pos = MutablePos()
		val iter = posToStateMap.long2ObjectEntrySet().fastIterator()
		while (iter.hasNext()) {
			val entry = iter.next()
			pos.setPos(entry.longKey)
			data[index(pos, size)] = entry.value
		}
	}
	
	private val data = Array(size.x * size.y * size.z) { fillState }
	
	override fun getState(pos: BlockPos): BlockState {
		return data[index(pos, size)]
	}
	
	override fun withState(pos: BlockPos, state: BlockState): ISegment {
		data[index(pos, size)] = state
		return this
	}
}
