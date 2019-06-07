package chylex.hee.game.world.feature.energyshrine.connection
import chylex.hee.game.world.structure.piece.IStructurePieceConnectionType

enum class EnergyShrineConnectionType : IStructurePieceConnectionType{
	CORRIDOR{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != TERMINAL
	},
	
	STAIR_BOTTOM{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = false // force stairs to always go down
	},
	
	STAIR_MIDDLE{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == STAIR_MIDDLE
	},
	
	STAIR_TOP{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == ROOM
	},
	
	ROOM{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target == CORRIDOR || target == ROOM || target == STAIR_BOTTOM
	},
	
	TERMINAL{
		override fun canBeAttachedTo(target: IStructurePieceConnectionType) = target != TERMINAL
	}
}
