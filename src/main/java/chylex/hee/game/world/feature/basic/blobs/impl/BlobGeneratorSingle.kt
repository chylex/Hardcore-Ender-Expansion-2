package chylex.hee.game.world.feature.basic.blobs.impl
import chylex.hee.game.world.feature.basic.blobs.IBlobGenerator
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.math.RandomRange
import java.util.Random

class BlobGeneratorSingle(
	private val radius: RandomRange
) : IBlobGenerator{
	override val size = Size(1 + (radius.max.ceilToInt() * 2))
	
	override fun generate(world: SegmentedWorld, rand: Random){
		IBlobGenerator.placeBlob(world, world.worldSize.centerPos, radius.nextDouble(rand))
	}
}
