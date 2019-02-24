package chylex.hee.game.world.feature.stronghold.connection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class StrongholdStairConnection(override val offset: BlockPos, override val facing: EnumFacing) : IStructurePieceConnection{
	override fun canConnectWith(other: IStructurePieceConnection): Boolean{
		return other !is StrongholdDoorConnection
	}
}
