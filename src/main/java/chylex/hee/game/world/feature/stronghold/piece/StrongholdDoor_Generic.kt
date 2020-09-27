package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.DOOR
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.WEST

class StrongholdDoor_Generic(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.OTHER){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(DOOR, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(DOOR, Pos(0, 0, centerZ), WEST)
	)
}
