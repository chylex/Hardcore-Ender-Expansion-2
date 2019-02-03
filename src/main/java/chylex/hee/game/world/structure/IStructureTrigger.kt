package chylex.hee.game.world.structure
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IStructureTrigger{
	fun realize(world: World, pos: BlockPos, rotation: Rotation)
}
