package chylex.hee.game.world.generation.blob.populators

import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.blob.BlobGenerator
import chylex.hee.game.world.generation.blob.IBlobPopulator
import chylex.hee.game.world.generation.ore.IOreTechnique
import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import java.util.Random

class BlobPopulatorOre(
	private val technique: IOreTechnique,
	private val placer: IBlockPlacer,
	
	private val attemptsPerBlob: Int,
	private val clustersPerBlob: (Random) -> Int,
) : IBlobPopulator {
	override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator) {
		val size = world.worldSize
		var clustersLeft = clustersPerBlob(rand).takeIf { it > 0 } ?: return
		
		for (attempt in 1..attemptsPerBlob) {
			val pos = Pos(
				rand.nextInt(0, size.maxX),
				rand.nextInt(0, size.maxY),
				rand.nextInt(0, size.maxZ)
			)
			
			if (technique.place(world, pos, placer) && --clustersLeft <= 0) {
				break
			}
		}
	}
}
