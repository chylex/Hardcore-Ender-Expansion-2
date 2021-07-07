package chylex.hee.game.block.logic

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

interface IBlockFireCatchOverride {
	fun tryCatchFire(world: World, pos: BlockPos, chance: Int, rand: Random)
}
