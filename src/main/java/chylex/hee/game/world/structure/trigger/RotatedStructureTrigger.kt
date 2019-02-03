package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class RotatedStructureTrigger(private val wrapped: IStructureTrigger, private val rotation: Rotation) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, rotation: Rotation){
		wrapped.realize(world, pos, rotation.add(this.rotation))
	}
}
