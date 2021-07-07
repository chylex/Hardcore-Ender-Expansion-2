package chylex.hee.game.world.generation.ore

import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.system.random.nextInt
import chylex.hee.util.math.BoundingBox
import chylex.hee.util.math.Pos
import net.minecraft.util.math.BlockPos
import java.util.Random

class OreGenerator(
	private val technique: IOreTechnique,
	private val placer: IBlockPlacer,
	
	private val chunkSize: Int,
	private val attemptsPerChunk: Int,
	private val clustersPerChunk: (Random) -> Int,
) {
	fun generate(world: SegmentedWorld, bounds: BoundingBox = world.worldSize.toBoundingBox(BlockPos.ZERO)): Int {
		val rand = world.rand
		
		val min = bounds.min
		val max = bounds.max
		val (sizeX, _, sizeZ) = bounds.size
		
		require(sizeX % chunkSize == 0 && sizeZ % chunkSize == 0) { "bounding box dimensions (X = $sizeX, Z = $sizeZ) must be multiples of chunk size ($chunkSize)" }
		
		var clustersGenerated = 0
		
		for (chunkX in 0 until sizeX step chunkSize) for (chunkZ in 0 until sizeZ step chunkSize) {
			var clustersLeft = clustersPerChunk(rand).takeIf { it > 0 } ?: continue
			
			for (attempt in 1..attemptsPerChunk) {
				val pos = Pos(
					min.x + chunkX + rand.nextInt(chunkSize),
					rand.nextInt(min.y, max.y),
					min.z + chunkZ + rand.nextInt(chunkSize)
				)
				
				if (technique.place(world, pos, placer)) {
					++clustersGenerated
					
					if (--clustersLeft <= 0) {
						break
					}
				}
			}
		}
		
		return clustersGenerated
	}
	
	fun generate(world: SegmentedWorld, heights: IntRange): Int {
		val bounds = BoundingBox(
			Pos(0, heights.first, 0),
			Pos(world.worldSize.maxX, heights.last, world.worldSize.maxZ)
		)
		
		return generate(world, bounds)
	}
}
