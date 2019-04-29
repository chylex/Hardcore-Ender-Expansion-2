package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.segments.SegmentFull
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.max
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

class BlobGenerator(private val patterns: WeightedList<BlobPattern>){
	constructor(pattern: BlobPattern) : this(weightedListOf(1 to pattern))
	
	private companion object{
		private val SCAFFOLDING = ModBlocks.SCAFFOLDING.defaultState
	}
	
	fun generate(world: SegmentedWorld, rand: Random, pos: BlockPos): Boolean{
		val pattern = patterns.generateItem(rand)
		val generator = pattern.pickGenerator(rand)
		val populators = pattern.pickPopulators(rand)
		
		val extraSize = populators.fold(BlockPos.ORIGIN){ acc, populator -> acc.max(populator.expandSizeBy) }
		val allocatedSize = generator.size.expand(extraSize)
		
		if (!allocatedSize.toBoundingBox(pos).isInside(world.worldSize.toBoundingBox(BlockPos.ORIGIN))){
			return false
		}
		
		val blobWorld = SegmentedWorld(rand, allocatedSize, allocatedSize){ SegmentFull(allocatedSize, SCAFFOLDING) }
		
		generator.generate(blobWorld, rand)
		runSmoothingPass(blobWorld, adjacentAirCount = 4)
		runSmoothingPass(blobWorld, adjacentAirCount = 5)
		
		for(populator in populators){
			populator.generate(blobWorld, rand)
		}
		
		for(offset in allocatedSize.minPos.allInBoxMutable(allocatedSize.maxPos)){
			val state = blobWorld.getState(offset)
			
			if (state !== SCAFFOLDING){
				world.setState(pos.add(offset), state)
			}
		}
		
		for((offset, trigger) in blobWorld.getTriggers()){
			world.addTrigger(pos.add(offset), trigger)
		}
		
		return true
	}
	
	private fun runSmoothingPass(blobWorld: SegmentedWorld, adjacentAirCount: Int){
		val size = blobWorld.worldSize
		
		for(pos in size.minPos.allInBoxMutable(size.maxPos)){
			if (blobWorld.getBlock(pos) === Blocks.END_STONE && Facing6.count { blobWorld.getState(pos.offset(it)) === SCAFFOLDING } >= adjacentAirCount){
				blobWorld.setState(pos, SCAFFOLDING)
			}
		}
	}
}
