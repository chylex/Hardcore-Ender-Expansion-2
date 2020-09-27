package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.game.world.math.Size
import java.util.Random

interface IBlobLayout{
	val size: Size
	fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator)
}
