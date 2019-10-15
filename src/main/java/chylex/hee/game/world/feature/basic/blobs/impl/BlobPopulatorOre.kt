package chylex.hee.game.world.feature.basic.blobs.impl
import chylex.hee.game.world.feature.basic.blobs.IBlobPopulator
import chylex.hee.game.world.feature.basic.ores.IOreTechnique
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import java.util.Random

class BlobPopulatorOre(
	private val technique: IOreTechnique,
	private val placer: IBlockPlacer,
	
	private val attemptsPerBlob: Int,
	private val clustersPerBlob: (Random) -> Int
) : IBlobPopulator{
	override fun generate(world: SegmentedWorld, rand: Random){
		val size = world.worldSize
		var clustersLeft = clustersPerBlob(rand).takeIf { it > 0 } ?: return
		
		for(attempt in 1..attemptsPerBlob){
			val pos = Pos(
				rand.nextInt(0, size.maxX),
				rand.nextInt(0, size.maxY),
				rand.nextInt(0, size.maxZ)
			)
			
			if (technique.place(world, pos, placer) && --clustersLeft <= 0){
				break
			}
		}
	}
}
