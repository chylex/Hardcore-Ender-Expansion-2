package chylex.hee.test.main

import chylex.hee.game.world.generation.structure.world.segments.ISegment
import chylex.hee.game.world.generation.structure.world.segments.SegmentFull
import chylex.hee.game.world.generation.structure.world.segments.SegmentMultiState
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.block.Blocks
import net.minecraft.util.registry.Bootstrap

fun main() {
	println("Initializing...")
	Bootstrap.register()
	
	val segments = mutableListOf<ISegment>()
	val thresholdField = SegmentMultiState::class.java.getDeclaredField("threshold").also { it.isAccessible = true }
	
	println("Waiting 20 seconds for data fixers...")
	Thread.sleep(20000L)
	println("Waiting 4 seconds to start memory profiling...")
	Thread.sleep(4000L)
	
	for (s in 8..128 step 12) {
		var segment: ISegment = SegmentMultiState(Size(s), Blocks.AIR).also(segments::add)
		val threshold = thresholdField.getInt(segment)
		
		println("Testing size $s (total ${s * s * s} threshold $threshold)")
		
		for ((index, pos) in Pos(0, 0, 0).allInBoxMutable(Pos(s - 1, s - 1, s - 1)).withIndex()) {
			if (index < threshold - 1) {
				segment = segment.withState(pos, Blocks.END_STONE.defaultState)
				continue
			}
			
			check(segment is SegmentMultiState)
			segment = segment.withState(pos, Blocks.END_STONE.defaultState).also(segments::add)
			check(segment is SegmentFull)
			break
		}
	}
	
	println("Done!")
	readLine()
	segments.toString()
}
