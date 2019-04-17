package chylex.hee.game.world.generation
import chylex.hee.game.world.util.Size
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

/**
 * Represents a mutable segment of blocks in a 3D space.
 */
interface ISegment{
	/**
	 * Returns the [IBlockState] at the specified position.
	 */
	fun getState(pos: BlockPos): IBlockState
	
	/**
	 * Changes the [IBlockState] at the specified position.
	 *
	 * If the current implementation supports such change, it should modify its internal state and return `this`.
	 * Otherwise, it must return a different implementation with the change applied.
	 */
	fun withState(pos: BlockPos, state: IBlockState): ISegment
	
	/**
	 * Utilities.
	 */
	companion object{
		inline fun index(x: Int, y: Int, z: Int, size: Size): Int{
			return x + (size.x * (y + (z * size.y)))
		}
		
		inline fun index(pos: BlockPos, size: Size): Int{
			return index(pos.x, pos.y, pos.z, size)
		}
	}
}
