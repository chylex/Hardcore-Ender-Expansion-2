package chylex.hee.game.world.generation.trigger

import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import net.minecraft.block.FlowingFluidBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld

class FluidStructureTrigger(private val block: FlowingFluidBlock) : IStructureTrigger {
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {
		world.setState(pos, transform(block.defaultState))
	}
	
	override fun realize(world: IServerWorld, pos: BlockPos, transform: Transform) {
		world.pendingFluidTicks.scheduleTick(pos, block.fluid, 0)
	}
}
