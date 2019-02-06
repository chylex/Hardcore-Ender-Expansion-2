package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.connection.StrongholdDoorConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.WEST

class StrongholdDoor_Generic(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdDoorConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdDoorConnection(Pos(0, 0, centerZ), WEST)
	)
}
