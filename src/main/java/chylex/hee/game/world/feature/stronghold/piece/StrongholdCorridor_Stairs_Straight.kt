package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.connection.StrongholdCorridorConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH

class StrongholdCorridor_Stairs_Straight(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdCorridorConnection(Pos(centerX, maxY - 4, 0), NORTH),
		StrongholdCorridorConnection(Pos(centerX, 0, maxZ), SOUTH)
	)
}
