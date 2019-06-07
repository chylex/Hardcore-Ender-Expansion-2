package chylex.hee.game.world.structure.piece
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

interface IStructurePieceConnection{
	val type: IStructurePieceConnectionType
	val offset: BlockPos
	val facing: EnumFacing
	
	@JvmDefault
	val isEvenWidth
		get() = false
}
