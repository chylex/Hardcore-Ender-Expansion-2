package chylex.hee.game.world.generation.blob.populators

import chylex.hee.game.world.generation.blob.BlobGenerator
import chylex.hee.game.world.generation.blob.IBlobPopulator
import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.util.math.Pos
import chylex.hee.util.math.floorToInt
import chylex.hee.util.random.RandomDouble
import java.util.Random

class BlobPopulatorShaveTop(
	private val height: RandomDouble,
) : IBlobPopulator {
	override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator) {
		val size = world.worldSize
		val shave = (size.maxY * height(rand)).floorToInt()
		
		for (pos in Pos(0, size.maxY - shave, 0).allInBoxMutable(size.maxPos)) {
			world.markUnused(pos)
		}
	}
}
