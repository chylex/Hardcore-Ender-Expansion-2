package chylex.hee.game.world.generation.blob

import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import net.minecraft.util.math.BlockPos
import java.util.Random

interface IBlobPopulator {
	val expandSizeBy: BlockPos
		get() = BlockPos.ZERO
	
	fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator)
}
