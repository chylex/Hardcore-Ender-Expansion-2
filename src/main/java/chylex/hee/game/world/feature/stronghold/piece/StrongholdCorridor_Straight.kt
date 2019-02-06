package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.connection.StrongholdCorridorConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH

class StrongholdCorridor_Straight(length: Int) : StrongholdAbstractPiece(){
	override val size = Size(5, 5, length)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdCorridorConnection(Pos(size.centerX, 0, 0), NORTH),
		StrongholdCorridorConnection(Pos(size.centerX, 0, size.maxZ), SOUTH)
	)
}
