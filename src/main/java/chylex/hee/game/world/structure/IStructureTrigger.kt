package chylex.hee.game.world.structure
import chylex.hee.game.world.util.Transform
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IStructureTrigger{
	fun realize(world: World, pos: BlockPos, transform: Transform)
}
