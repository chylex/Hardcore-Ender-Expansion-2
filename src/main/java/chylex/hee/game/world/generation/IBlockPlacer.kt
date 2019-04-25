package chylex.hee.game.world.generation
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos

interface IBlockPlacer{
	fun place(world: SegmentedWorld, pos: BlockPos): Boolean
	
	// Implementations
	
	open class BlockPlacer(private val fill: Block) : IBlockPlacer{
		override fun place(world: SegmentedWorld, pos: BlockPos): Boolean{
			if (!world.isInside(pos)){
				return false
			}
			
			world.setBlock(pos, fill)
			return true
		}
	}
	
	open class BlockReplacer(private val fill: Block, private val replace: Block) : IBlockPlacer{
		override fun place(world: SegmentedWorld, pos: BlockPos): Boolean{
			if (!world.isInside(pos) || world.getBlock(pos) !== replace){
				return false
			}
			
			world.setBlock(pos, fill)
			return true
		}
	}
}
