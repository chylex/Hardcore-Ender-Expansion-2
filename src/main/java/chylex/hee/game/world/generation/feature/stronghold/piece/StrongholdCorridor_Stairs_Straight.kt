package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.STAIR
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH

class StrongholdCorridor_Stairs_Straight(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.CORRIDOR) {
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(STAIR, Pos(centerX, maxY - 4, 0), NORTH),
		StrongholdConnection(STAIR, Pos(centerX, 0, maxZ), SOUTH)
	)
}
