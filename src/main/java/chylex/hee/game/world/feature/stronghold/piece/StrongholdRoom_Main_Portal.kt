package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class StrongholdRoom_Main_Portal(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(centerX, 0, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 6, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(centerX, 6, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdRoomConnection(Pos(maxX, 6, centerZ), EAST),
		StrongholdRoomConnection(Pos(0, 0, centerZ), WEST),
		StrongholdRoomConnection(Pos(0, 6, centerZ), WEST)
	)
	
	// TODO global silverfish spawner
}
