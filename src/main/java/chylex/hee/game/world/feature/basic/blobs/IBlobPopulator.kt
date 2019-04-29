package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.game.world.generation.SegmentedWorld
import net.minecraft.util.math.BlockPos
import java.util.Random

interface IBlobPopulator{
	@JvmDefault
	val expandSizeBy: BlockPos
		get() = BlockPos.ORIGIN
	
	fun generate(world: SegmentedWorld, rand: Random)
}
