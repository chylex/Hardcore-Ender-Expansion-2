package chylex.hee.game.world.generation.blob

import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import chylex.hee.util.math.Size
import java.util.Random

interface IBlobLayout {
	val size: Size
	fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator)
}
