package chylex.hee.game.block.util
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IBlockHarvestDropsOverride{
	fun onHarvestDrops(state: BlockState, world: World, pos: BlockPos)
	
	companion object{
		@JvmStatic
		@Suppress("unused")
		fun checkHarvest(state: BlockState, world: World, pos: BlockPos, stack: ItemStack): Boolean{
			val item = stack.item as? IBlockHarvestDropsOverride ?: return false
			
			item.onHarvestDrops(state, world, pos)
			return true
		}
	}
}
