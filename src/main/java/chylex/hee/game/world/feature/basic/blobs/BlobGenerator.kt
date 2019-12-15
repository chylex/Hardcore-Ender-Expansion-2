package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.game.world.feature.basic.blobs.BlobSmoothing.FULL
import chylex.hee.game.world.feature.basic.blobs.BlobSmoothing.MILD
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.allInCenteredSphereMutable
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.facades.Facing6
import chylex.hee.system.util.max
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import java.util.Random

object BlobGenerator{
	val BASE: Block = Blocks.END_STONE
	
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
		
		val blobWorld = ScaffoldedWorld(rand, allocatedSize)
		
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
		
		blobWorld.cloneInto(world, origin)
		return true
	}
	
	private fun runSmoothingPass(blobWorld: ScaffoldedWorld, adjacentAirCount: Int){
		for(pos in blobWorld.allPositionsMutable){
			if (blobWorld.getBlock(pos) === BASE && Facing6.count { facing -> pos.offset(facing).let { !blobWorld.isInside(it) || blobWorld.isUnused(it) } } >= adjacentAirCount){
				blobWorld.markUnused(pos)
			}
		}
	}
}
