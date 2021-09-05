package chylex.hee.game.world.generation.structure.world

import chylex.hee.HEE
import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.world.segments.ISegment
import chylex.hee.game.world.generation.structure.world.segments.ISegment.Companion.index
import chylex.hee.game.world.util.Transform
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import chylex.hee.util.math.ceilToInt
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

open class SegmentedWorld(override val rand: Random, val worldSize: Size, segmentSize: Size, defaultSegmentFactory: (Size) -> ISegment) : IStructureWorld {
	private val segmentSizeX = segmentSize.x
	private val segmentSizeY = segmentSize.y
	private val segmentSizeZ = segmentSize.z
	
	private val segmentCounts = Size(
		(worldSize.x.toFloat() / segmentSizeX).ceilToInt(),
		(worldSize.y.toFloat() / segmentSizeY).ceilToInt(),
		(worldSize.z.toFloat() / segmentSizeZ).ceilToInt()
	)
	
	private val segments = Array(segmentCounts.x * segmentCounts.y * segmentCounts.z) { defaultSegmentFactory(segmentSize) }
	private val triggers = mutableListOf<Pair<BlockPos, IStructureTrigger>>()
	
	private fun getSegmentIndex(pos: BlockPos): Int {
		return index(pos.x / segmentSizeX, pos.y / segmentSizeY, pos.z / segmentSizeZ, segmentCounts)
	}
	
	private fun getSegmentOffset(pos: BlockPos): BlockPos {
		return Pos(pos.x % segmentSizeX, pos.y % segmentSizeY, pos.z % segmentSizeZ)
	}
	
	fun isInside(pos: BlockPos): Boolean {
		return pos.x in 0..worldSize.maxX && pos.y in 0..worldSize.maxY && pos.z in 0..worldSize.maxZ
	}
	
	private fun warnNotInside(pos: BlockPos) {
		HEE.log.warn("[SegmentedWorld] attempted to access position outside bounds: $pos is outside $worldSize")
		
		if (HEE.debug) {
			Thread.dumpStack()
		}
	}
	
	override fun getState(pos: BlockPos): BlockState {
		if (!isInside(pos)) {
			warnNotInside(pos)
			return Blocks.AIR.defaultState
		}
		
		val segmentIndex = getSegmentIndex(pos)
		val segmentOffset = getSegmentOffset(pos)
		return segments[segmentIndex].getState(segmentOffset)
	}
	
	override fun setState(pos: BlockPos, state: BlockState) {
		if (!isInside(pos)) {
			warnNotInside(pos)
			return
		}
		
		val segmentIndex = getSegmentIndex(pos)
		val segmentOffset = getSegmentOffset(pos)
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
