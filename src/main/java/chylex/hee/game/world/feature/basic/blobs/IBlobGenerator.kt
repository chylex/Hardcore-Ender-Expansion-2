package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.util.Size
import java.util.Random

interface IBlobGenerator{
	val size: Size
	fun generate(world: SegmentedWorld, rand: Random)
}
