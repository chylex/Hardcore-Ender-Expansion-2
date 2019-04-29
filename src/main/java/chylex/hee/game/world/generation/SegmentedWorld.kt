package chylex.hee.game.world.generation
import chylex.hee.HEE
import chylex.hee.game.world.generation.ISegment.Companion.index
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Size
import chylex.hee.system.Debug
import chylex.hee.system.util.Pos
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

class SegmentedWorld(override val rand: Random, val worldSize: Size, private val segmentSize: Size, defaultSegmentFactory: (Size) -> ISegment) : IStructureWorld{
	private val segmentCounts = Size(
		(worldSize.x.toFloat() / segmentSize.x).ceilToInt(),
		(worldSize.y.toFloat() / segmentSize.y).ceilToInt(),
		(worldSize.z.toFloat() / segmentSize.z).ceilToInt()
	)
	
	private val segments = Array(segmentCounts.x * segmentCounts.y * segmentCounts.z){ defaultSegmentFactory(segmentSize) }
	private val triggers = mutableListOf<Pair<BlockPos, IStructureTrigger>>()
	
	private fun mapPos(pos: BlockPos): Pair<Int, BlockPos>?{
		if (!isInside(pos)){
			HEE.log.warn("[SegmentedWorld] attempted to access position outside bounds: $pos is outside $worldSize")
			
			if (Debug.enabled){
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
	
	fun isInside(pos: BlockPos): Boolean{
		return pos.x in 0..worldSize.maxX && pos.y in 0..worldSize.maxY && pos.z in 0..worldSize.maxZ
	}
	
	override fun getState(pos: BlockPos): IBlockState{
		val (segmentIndex, segmentOffset) = mapPos(pos) ?: return Blocks.AIR.defaultState
		return segments[segmentIndex].getState(segmentOffset)
	}
	
	override fun setState(pos: BlockPos, state: IBlockState){
		val (segmentIndex, segmentOffset) = mapPos(pos) ?: return
		segments[segmentIndex] = segments[segmentIndex].withState(segmentOffset, state)
	}
	
	fun getTriggers(): List<Pair<BlockPos, IStructureTrigger>>{
		return triggers
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger){
		if (isInside(pos)){
			triggers.add(pos to trigger)
		}
	}
	
	override fun finalize(){
		// unused
	}
}
