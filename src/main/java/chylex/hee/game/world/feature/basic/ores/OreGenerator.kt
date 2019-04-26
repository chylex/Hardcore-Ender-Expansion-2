package chylex.hee.game.world.feature.basic.ores
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.util.BoundingBox
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import net.minecraft.util.math.BlockPos
import java.util.Random

class OreGenerator(
	private val technique: IOreTechnique,
	private val placer: IBlockPlacer,
	
	private val chunkSize: Int,
	private val attemptsPerChunk: Int,
	private val clustersPerChunk: (Random) -> Int
){
	fun generate(world: SegmentedWorld, bounds: BoundingBox = world.worldSize.toBoundingBox(BlockPos.ORIGIN)){
		val rand = world.rand
		
		val min = bounds.min
		val max = bounds.max
		val size = bounds.size
		
		for(chunkX in 0 until size.x step chunkSize) for(chunkZ in 0 until size.z step chunkSize){
			var clustersLeft = clustersPerChunk(rand)
			
			for(attempt in 1..attemptsPerChunk){
				val pos = Pos(
					min.x + chunkX + rand.nextInt(chunkSize),
					rand.nextInt(min.y, max.y),
					min.z + chunkZ + rand.nextInt(chunkSize)
				)
				
				if (technique.place(world, pos, placer) && --clustersLeft <= 0){
					break
				}
			}
		}
	}
	
	fun generate(world: SegmentedWorld, heights: IntRange){
		val bounds = BoundingBox(
			Pos(0, heights.start, 0),
			Pos(world.worldSize.maxX, heights.endInclusive, world.worldSize.maxZ)
		)
		
		generate(world, bounds)
	}
}
