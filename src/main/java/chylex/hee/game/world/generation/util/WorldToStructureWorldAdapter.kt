package chylex.hee.game.world.generation.util

import chylex.hee.game.block.BlockGloomtorch
import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.trigger.FluidStructureTrigger
import chylex.hee.game.world.util.FLAG_REPLACE_NO_DROPS
import chylex.hee.game.world.util.FLAG_SYNC_CLIENT
import chylex.hee.game.world.util.Transform
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.setState
import net.minecraft.block.AbstractButtonBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.FlowingFluidBlock
import net.minecraft.block.RedstoneWireBlock
import net.minecraft.block.TorchBlock
import net.minecraft.block.VineBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld
import java.util.Random

class WorldToStructureWorldAdapter(private val world: IServerWorld, override val rand: Random, private val offset: BlockPos) : IStructureWorld {
	private fun requiresSecondPass(block: Block) = when (block) {
		is VineBlock,
		is TorchBlock,
		is BlockGloomtorch,
		is AbstractButtonBlock,
		is RedstoneWireBlock -> true
		
		else -> false
	}
	
	private val secondPass = mutableMapOf<BlockPos, BlockState>()
	
	override fun getState(pos: BlockPos): BlockState {
		return secondPass[pos] ?: pos.add(offset).getState(world)
	}
	
	override fun setState(pos: BlockPos, state: BlockState) {
		val block = state.block
		
		if (requiresSecondPass(block)) {
			secondPass[pos] = state
			return
		}
		else {
			secondPass.remove(pos) // avoids attempting to generate 2 blocks in one position if one requires second pass
		}
		
		val worldPos = pos.add(offset)
		worldPos.setState(world, state, FLAG_SYNC_CLIENT or FLAG_REPLACE_NO_DROPS)
		
		if (block is FlowingFluidBlock) {
			FluidStructureTrigger(block).realize(world, worldPos, Transform.NONE)
		}
	}
	
	override fun addTrigger(pos: BlockPos, trigger: IStructureTrigger) {
		trigger.setup(this, pos, Transform.NONE)
		trigger.realize(world, pos.add(offset), Transform.NONE)
	}
	
	override fun finalize() {
		for ((pos, state) in secondPass) {
			pos.add(offset).setState(world, state, FLAG_SYNC_CLIENT or FLAG_REPLACE_NO_DROPS)
		}
		
		secondPass.clear()
	}
}
