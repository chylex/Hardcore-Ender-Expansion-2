package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

fun interface ISetBlockStateFromNeighbor {
	fun getNewState(state: BlockState, world: IWorld, pos: BlockPos, neighborFacing: Direction, neighborPos: BlockPos): BlockState
}
