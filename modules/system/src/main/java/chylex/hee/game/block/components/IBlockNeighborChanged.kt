package chylex.hee.game.block.components

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface IBlockNeighborChanged {
	fun onNeighborChanged(state: BlockState, world: World, pos: BlockPos, oldNeighborBlock: Block, newNeighborBlock: Block, neighborPos: BlockPos)
}
