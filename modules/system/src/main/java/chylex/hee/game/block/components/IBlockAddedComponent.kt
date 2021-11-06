package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface IBlockAddedComponent {
	fun onAdded(state: BlockState, world: World, pos: BlockPos)
}
