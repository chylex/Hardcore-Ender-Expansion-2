package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType.CORRIDOR
import chylex.hee.game.world.feature.stronghold.connection.StrongholdStairConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH

class StrongholdCorridor_Stairs_Straight(file: String) : StrongholdAbstractPieceFromFile(file, CORRIDOR){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdStairConnection(Pos(centerX, maxY - 4, 0), NORTH),
		StrongholdStairConnection(Pos(centerX, 0, maxZ), SOUTH)
	)
}
