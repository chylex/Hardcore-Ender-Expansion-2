package chylex.hee.game.world.generation
import chylex.hee.game.world.generation.ISegment.Companion.index
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

class SegmentedWorld(val worldSize: Size, private val segmentSize: Size, defaultSegmentFactory: (Size) -> ISegment){
	private val segmentCounts = Size(
		(worldSize.x.toFloat() / segmentSize.x).ceilToInt(),
		(worldSize.y.toFloat() / segmentSize.y).ceilToInt(),
		(worldSize.z.toFloat() / segmentSize.z).ceilToInt()
	)
	
	private val segments = Array(segmentCounts.x * segmentCounts.y * segmentCounts.z){ defaultSegmentFactory(segmentSize) }
	
	init{
		if (worldSize.x % 16 != 0 || worldSize.z % 16 != 0){
			throw IllegalArgumentException("segmented world size must be chunk-aligned")
		}
	}
	
	private fun mapPos(pos: BlockPos): Pair<Int, BlockPos>{
		val (x, y, z) = pos
		val (sizeX, sizeY, sizeZ) = segmentSize
		
		if (x < 0 || y < 0 || z < 0 || x > worldSize.maxX || y > worldSize.maxY || z > worldSize.maxZ){
			throw IndexOutOfBoundsException("position is out of bounds: $pos")
		}
		
		val segmentIndex = index(x / sizeX, y / sizeY, z / sizeZ, segmentCounts)
		val segmentOffset = Pos(x % sizeX, y % sizeY, z % sizeZ)
		
		return Pair(segmentIndex, segmentOffset)
	}
	
	fun getState(pos: BlockPos): IBlockState{
		val (segmentIndex, segmentOffset) = mapPos(pos)
		return segments[segmentIndex].getState(segmentOffset)
	}
	
	fun setState(pos: BlockPos, state: IBlockState){
		val (segmentIndex, segmentOffset) = mapPos(pos)
		segments[segmentIndex] = segments[segmentIndex].withState(segmentOffset, state)
	}
}
