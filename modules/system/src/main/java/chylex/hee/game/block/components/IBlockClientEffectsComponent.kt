package chylex.hee.game.block.components

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

interface IBlockClientEffectsComponent {
	fun randomTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {}
}
