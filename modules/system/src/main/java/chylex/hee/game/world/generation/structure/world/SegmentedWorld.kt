package chylex.hee.game.world.generation.structure.world

import chylex.hee.HEE
import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.world.segments.ISegment
import chylex.hee.game.world.generation.structure.world.segments.ISegment.Companion.index
import chylex.hee.game.world.util.Transform
import chylex.hee.system.Debug
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import chylex.hee.util.math.component3
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

open class SegmentedWorld(override val rand: Random, val worldSize: Size, private val segmentSize: Size, defaultSegmentFactory: (Size) -> ISegment) : IStructureWorld {
	private val segmentCounts = Size(
		(worldSize.x.toFloat() / segmentSize.x).ceilToInt(),
		(worldSize.y.toFloat() / segmentSize.y).ceilToInt(),
		(worldSize.z.toFloat() / segmentSize.z).ceilToInt()
	)
	
	private val segments = Array(segmentCounts.x * segmentCounts.y * segmentCounts.z) { defaultSegmentFactory(segmentSize) }
	private val triggers = mutableListOf<Pair<BlockPos, IStructureTrigger>>()
	
	private fun mapPos(pos: BlockPos): Pair<Int, BlockPos>? {
		if (!isInside(pos)) {
			HEE.log.warn("[SegmentedWorld] attempted to access position outside bounds: $pos is outside $worldSize")
			
			if (Debug.enabled) {
				Thread.dumpStack()
			}
			
			return null
		}
		
		val (x, y, z) = pos
		val (sizeX, sizeY, sizeZ) = segmentSize
		
		val segmentIndex = index(x / sizeX, y / sizeY, z / sizeZ, segmentCounts)
		val segmentOffset = Pos(x % sizeX, y % sizeY, z % sizeZ)
		
		return Pair(segmentIndex, segmentOffset)
	}
	
	fun isInside(pos: BlockPos): Boolean {
		return pos.x in 0..worldSize.maxX && pos.y in 0..worldSize.maxY && pos.z in 0..worldSize.maxZ
	}
	
	override fun getState(pos: BlockPos): BlockState {
		val (segmentIndex, segmentOffset) = mapPos(pos) ?: return Blocks.AIR.defaultState
		return segments[segmentIndex].getState(segmentOffset)
	}
	
	override fun setState(pos: BlockPos, state: BlockState) {
		val (segmentIndex, segmentOffset) = mapPos(pos) ?: return
		segments[segmentIndex] = segments[segmentIndex].withState(segmentOffset, state)
	}
	
	fun getTriggers(): List<Pair<BlockPos, IStructureTrigger>> {
		return triggers
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger) {
		if (isInside(pos)) {
			trigger.setup(this, pos, Transform.NONE)
			triggers.add(pos to trigger)
		}
	}
	
	override fun finalize() {
		// unused
	}
}
