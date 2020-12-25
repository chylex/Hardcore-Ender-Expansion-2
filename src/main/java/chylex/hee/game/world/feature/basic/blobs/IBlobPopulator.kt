package chylex.hee.game.world.feature.basic.blobs

import chylex.hee.game.world.generation.ScaffoldedWorld
import net.minecraft.util.math.BlockPos
import java.util.Random

interface IBlobPopulator {
	@JvmDefault
	val expandSizeBy: BlockPos
		get() = BlockPos.ZERO
	
	fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator)
}
