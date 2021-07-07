package chylex.hee.game.world.generation.blob.layouts

import chylex.hee.game.world.generation.blob.BlobGenerator
import chylex.hee.game.world.generation.blob.IBlobLayout
import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import chylex.hee.util.math.Size
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.random.RandomDouble
import java.util.Random

class BlobLayoutSingle(
	private val radius: RandomDouble,
) : IBlobLayout {
	override val size = Size(1 + (radius.max.ceilToInt() * 2))
	
	override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator) {
		generator.place(world, world.worldSize.centerPos, radius(rand))
	}
}
