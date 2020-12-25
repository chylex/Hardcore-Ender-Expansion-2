package chylex.hee.game.world.feature.stronghold.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.STAIR
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH

class StrongholdCorridor_Stairs_Straight(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.CORRIDOR) {
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(STAIR, Pos(centerX, maxY - 4, 0), NORTH),
		StrongholdConnection(STAIR, Pos(centerX, 0, maxZ), SOUTH)
	)
}
