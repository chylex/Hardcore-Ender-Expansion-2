package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TransformedStructureTrigger(private val wrapped: IStructureTrigger, private val transform: Transform) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		wrapped.setup(world, pos, this.transform.applyTo(transform))
	}
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		wrapped.realize(world, pos, this.transform.applyTo(transform))
	}
}
