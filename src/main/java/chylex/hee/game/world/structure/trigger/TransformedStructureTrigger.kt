package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.util.Transform
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TransformedStructureTrigger(private val wrapped: IStructureTrigger, private val transform: Transform) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		wrapped.realize(world, pos, this.transform.applyTo(transform))
	}
}
