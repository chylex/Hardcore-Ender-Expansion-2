package chylex.hee.game.block.logic
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IBlockHarvestDropsOverride{
	fun onHarvestDrops(state: BlockState, world: World, pos: BlockPos)
}
