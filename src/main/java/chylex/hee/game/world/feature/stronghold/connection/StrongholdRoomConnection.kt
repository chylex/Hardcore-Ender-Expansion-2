package chylex.hee.game.world.feature.stronghold.connection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class StrongholdRoomConnection(override val offset: BlockPos, override val facing: EnumFacing) : IStructurePieceConnection{
	override fun canBeAttachedTo(target: IStructurePieceConnection): Boolean{
		return target is StrongholdCorridorConnection || target is StrongholdDoorConnection
	}
}
