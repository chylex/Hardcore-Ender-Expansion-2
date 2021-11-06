package chylex.hee.game.block.properties

import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

sealed class TickSchedule {
	abstract fun schedule(world: World, pos: BlockPos, block: Block)
	
	object Never : TickSchedule() {
		override fun schedule(world: World, pos: BlockPos, block: Block) {}
	}
	
	class InTicks(private val ticks: Int) : TickSchedule() {
		override fun schedule(world: World, pos: BlockPos, block: Block) {
			world.pendingBlockTicks.scheduleTick(pos, block, ticks)
		}
	}
}
