package chylex.hee.game.world.structure.piece
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

interface IStructurePieceConnection{
	val offset: BlockPos
	val facing: EnumFacing
	
	fun canConnectWith(other: IStructurePieceConnection): Boolean
}
