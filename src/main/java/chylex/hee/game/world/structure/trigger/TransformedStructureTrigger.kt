package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class TransformedStructureTrigger(override val wrappedInstance: IStructureTrigger, private val transform: Transform) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		wrappedInstance.setup(world, pos, this.transform.applyTo(transform))
	}
	
	override fun realize(world: IWorld, pos: BlockPos, transform: Transform){
		wrappedInstance.realize(world, pos, this.transform.applyTo(transform))
	}
	
	override fun rewrapInstance(trigger: IStructureTrigger): IStructureTrigger{
		return TransformedStructureTrigger(trigger, transform)
	}
}
