package chylex.hee.game.world.generation
import chylex.hee.game.world.math.Size
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

/**
 * Represents a mutable segment of blocks in a 3D space.
 */
interface ISegment{
	/**
	 * Returns the [BlockState] at the specified position.
	 */
	fun getState(pos: BlockPos): BlockState
	
	/**
	 * Changes the [BlockState] at the specified position.
	 *
	 * If the current implementation supports such change, it should modify its internal state and return `this`.
	 * Otherwise, it must return a different implementation with the change applied.
	 */
	fun withState(pos: BlockPos, state: BlockState): ISegment
	
	/**
	 * Utilities.
	 */
	@Suppress("NOTHING_TO_INLINE")
	companion object{
		inline fun index(x: Int, y: Int, z: Int, size: Size): Int{
			return x + (size.x * (y + (z * size.y)))
		}
		
		inline fun index(pos: BlockPos, size: Size): Int{
			return index(pos.x, pos.y, pos.z, size)
		}
	}
}
