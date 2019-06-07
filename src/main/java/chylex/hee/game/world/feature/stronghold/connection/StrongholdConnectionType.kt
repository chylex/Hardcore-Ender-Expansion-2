package chylex.hee.game.world.feature.stronghold.connection
import chylex.hee.game.world.structure.piece.IStructurePieceConnectionType

enum class StrongholdConnectionType : IStructurePieceConnectionType{
	CORRIDOR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = true
	},
	
	STAIR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != DOOR
	},
	
	DOOR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != DOOR && target != STAIR
	},
	
	ROOM{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == CORRIDOR || target == DOOR
	},
	
	DEAD_END{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == CORRIDOR
	}
}
