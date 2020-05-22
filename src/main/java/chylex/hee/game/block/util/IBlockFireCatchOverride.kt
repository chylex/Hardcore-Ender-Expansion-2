package chylex.hee.game.block.util
import chylex.hee.system.util.getBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

interface IBlockFireCatchOverride{
	fun tryCatchFire(world: World, pos: BlockPos, chance: Int, rand: Random)
	
	companion object{
		@JvmStatic
		@Suppress("unused")
		fun tryCatchFire(world: World, pos: BlockPos, chance: Int, rand: Random): Boolean{
			val block = pos.getBlock(world) as? IBlockFireCatchOverride ?: return false
			
			block.tryCatchFire(world, pos, chance, rand)
			return true
		}
	}
}
