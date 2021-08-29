package chylex.hee.game.world.generation.structure.world.segments

import chylex.hee.util.math.Size
import chylex.hee.util.math.floorToInt
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import kotlin.math.pow

/**
 * A segment mostly filled with one type of block state, but allowing for a few [exceptions].
 * When the amount of exceptions reaches a [threshold] based on the segment size, it gets converted to [SegmentFull].
 */
class SegmentMultiState(private val size: Size, private val fillState: BlockState) : ISegment {
	constructor(size: Size, block: Block) : this(size, block.defaultState)
	
	/**
	 * The threshold power was determined using the WorldSegmentProfiling test.
	 * The segment gets converted when the amount of memory used by the [exceptions] map reaches roughly half of the memory used by the [SegmentFull] array.
	 */
	private val threshold = (size.x * size.y * size.z).toDouble().pow(0.825).floorToInt()
	private val exceptions = Long2ObjectOpenHashMap<BlockState>(threshold, 0.75F).apply { defaultReturnValue(fillState) }
	
	override fun getState(pos: BlockPos): BlockState {
		return exceptions.get(pos.toLong())
	}
	
	override fun withState(pos: BlockPos, state: BlockState): ISegment {
		if (state == fillState) {
			exceptions.remove(pos.toLong())
			return this
		}
		
		@Suppress("ReplacePutWithAssignment")
		exceptions.put(pos.toLong(), state) // kotlin indexer boxes the values
		
		return if (exceptions.size < threshold)
			this
		else
			SegmentFull(size, fillState, exceptions)
	}
}
