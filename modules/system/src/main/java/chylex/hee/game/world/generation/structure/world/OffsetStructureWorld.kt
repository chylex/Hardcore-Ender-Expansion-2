package chylex.hee.game.world.generation.structure.world

import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class OffsetStructureWorld(private val wrapped: IStructureWorld, private val offset: BlockPos) : IStructureWorld {
	override val rand = wrapped.rand
	
	override fun getState(pos: BlockPos): BlockState {
		return wrapped.getState(pos.add(offset))
	}
	
	override fun setState(pos: BlockPos, state: BlockState) {
		wrapped.setState(pos.add(offset), state)
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger) {
		wrapped.addTrigger(pos.add(offset), trigger)
	}
	
	override fun finalize() {
		wrapped.finalize()
	}
}
