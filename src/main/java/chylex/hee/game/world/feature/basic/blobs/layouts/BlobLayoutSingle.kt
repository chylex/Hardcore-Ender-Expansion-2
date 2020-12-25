package chylex.hee.game.world.feature.basic.blobs.layouts

import chylex.hee.game.world.feature.basic.blobs.BlobGenerator
import chylex.hee.game.world.feature.basic.blobs.IBlobLayout
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.game.world.math.Size
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.random.RandomDouble
import java.util.Random

class BlobLayoutSingle(
	private val radius: RandomDouble,
) : IBlobLayout {
	override val size = Size(1 + (radius.max.ceilToInt() * 2))
	
	override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator) {
		generator.place(world, world.worldSize.centerPos, radius(rand))
	}
}
