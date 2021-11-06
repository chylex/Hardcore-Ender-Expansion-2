package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

fun interface IBlockRandomTickComponent {
	fun onTick(state: BlockState, world: World, pos: BlockPos, rand: Random)
}
