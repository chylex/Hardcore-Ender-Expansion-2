package chylex.hee.game.world.structure
import chylex.hee.game.world.util.Transform
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IStructureTrigger{
	fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform)
	fun realize(world: World, pos: BlockPos, transform: Transform)
}
