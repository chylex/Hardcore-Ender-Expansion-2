package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.game.world.feature.basic.blobs.BlobSmoothing.FULL
import chylex.hee.game.world.feature.basic.blobs.BlobSmoothing.MILD
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.segments.SegmentFull
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.allInCenteredSphereMutable
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.max
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

object BlobGenerator{
	private val SCAFFOLDING = ModBlocks.SCAFFOLDING.defaultState
	private val BASE = Blocks.END_STONE
	
	fun place(world: SegmentedWorld, center: BlockPos, radius: Double, block: Block = BASE): Boolean{
		val offset = radius.ceilToInt()
		
		if (!world.isInside(center) || Facing6.any { !world.isInside(center.offset(it, offset)) }){
			return false
		}
		
		for(pos in center.allInCenteredSphereMutable(radius)){
			world.setBlock(pos, block)
		}
		
		return true
	}
	
	fun generate(world: SegmentedWorld, rand: Random, center: BlockPos, smoothing: BlobSmoothing, pattern: BlobPattern): Boolean{
		val generator = pattern.pickGenerator(rand)
		val populators = pattern.pickPopulators(rand)
		
		val extraSize = populators.fold(BlockPos.ORIGIN){ acc, populator -> acc.max(populator.expandSizeBy) }
		val allocatedSize = generator.size.expand(extraSize)
		
		val origin = center.subtract(allocatedSize.centerPos)
		
		if (!allocatedSize.toBoundingBox(origin).isInside(world.worldSize.toBoundingBox(BlockPos.ORIGIN))){
			return false
		}
		
		val blobWorld = SegmentedWorld(rand, allocatedSize, allocatedSize){ SegmentFull(allocatedSize, SCAFFOLDING) }
		
		generator.generate(blobWorld, rand)
		
		if (smoothing == FULL){
			runSmoothingPass(blobWorld, adjacentAirCount = 4)
		}
		
		if (smoothing == FULL || smoothing == MILD){
			runSmoothingPass(blobWorld, adjacentAirCount = 5)
		}
		
		for(populator in populators){
			populator.generate(blobWorld, rand)
		}
		
		for(offset in allocatedSize.minPos.allInBoxMutable(allocatedSize.maxPos)){
			val state = blobWorld.getState(offset)
			
			if (state !== SCAFFOLDING){
				world.setState(origin.add(offset), state)
			}
		}
		
		for((offset, trigger) in blobWorld.getTriggers()){
			world.addTrigger(origin.add(offset), trigger)
		}
		
		return true
	}
	
	private fun runSmoothingPass(blobWorld: SegmentedWorld, adjacentAirCount: Int){
		val size = blobWorld.worldSize
		
		for(pos in size.minPos.allInBoxMutable(size.maxPos)){
			if (blobWorld.getBlock(pos) === BASE && Facing6.count { facing -> pos.offset(facing).let { !blobWorld.isInside(it) || blobWorld.getState(it) === SCAFFOLDING } } >= adjacentAirCount){
				blobWorld.setState(pos, SCAFFOLDING)
			}
		}
	}
}
