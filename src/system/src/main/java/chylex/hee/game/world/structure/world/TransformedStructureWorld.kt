package chylex.hee.game.world.structure.world

import chylex.hee.game.world.math.Size
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld

class TransformedStructureWorld(private val wrapped: IStructureWorld, private val size: Size, private val transform: Transform) : IStructureWorld {
	override val rand = wrapped.rand
	
	private val reverseTransform = transform.reverse
	
	override fun getState(pos: BlockPos): BlockState {
		return reverseTransform(wrapped.getState(transform(pos, size)))
	}
	
	override fun setState(pos: BlockPos, state: BlockState) {
		wrapped.setState(transform(pos, size), transform(state))
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger) {
		wrapped.addTrigger(transform(pos, size), TransformedStructureTrigger(trigger, transform))
	}
	
	override fun finalize() {
		wrapped.finalize()
	}
	
	private class TransformedStructureTrigger(override val wrappedInstance: IStructureTrigger, private val transform: Transform) : IStructureTrigger {
		override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {
			wrappedInstance.setup(world, pos, this.transform.applyTo(transform))
		}
		
		override fun realize(world: IServerWorld, pos: BlockPos, transform: Transform) {
			wrappedInstance.realize(world, pos, this.transform.applyTo(transform))
		}
		
		override fun rewrapInstance(trigger: IStructureTrigger): IStructureTrigger {
			return TransformedStructureTrigger(trigger, transform)
		}
	}
}
