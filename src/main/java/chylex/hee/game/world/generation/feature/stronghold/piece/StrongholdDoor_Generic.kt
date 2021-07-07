package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.DOOR
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.WEST

class StrongholdDoor_Generic(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.OTHER) {
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(DOOR, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(DOOR, Pos(0, 0, centerZ), WEST)
	)
}
