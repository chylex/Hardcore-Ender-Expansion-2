package chylex.hee.game.block.logic

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

interface IBlockDynamicHardness {
	fun getBlockHardness(world: IBlockReader, pos: BlockPos, state: BlockState, originalHardness: Float): Float
}
