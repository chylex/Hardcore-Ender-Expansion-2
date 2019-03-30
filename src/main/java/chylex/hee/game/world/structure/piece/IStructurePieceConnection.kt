package chylex.hee.game.world.structure.piece
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

interface IStructurePieceConnection{
	val offset: BlockPos
	val facing: EnumFacing
	
	@JvmDefault
	val isEvenWidth
		get() = false
	
	/**
	 * Returns true if the current connection, which is always on a new piece, can be connected to the [target] connection of an already generated piece.
	 * The relation defined by the method does not have to be symmetric, which allows for one-directional connections.
	 */
	fun canBeAttachedTo(target: IStructurePieceConnection): Boolean
}
