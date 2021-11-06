package chylex.hee.game.block.logic

import chylex.hee.game.block.interfaces.IBlockInterface
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

interface IBlockDynamicHardness : IBlockInterface {
	fun getBlockHardness(world: IBlockReader, pos: BlockPos, state: BlockState, originalHardness: Float): Float
}
