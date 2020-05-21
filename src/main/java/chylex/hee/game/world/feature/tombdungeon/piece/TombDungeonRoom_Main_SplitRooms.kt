package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.util.Pos

class TombDungeonRoom_Main_SplitRooms(file: String, isFancy: Boolean) : TombDungeonRoom(file, isFancy){
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, 0), NORTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		placeChest(world, instance, Pos(maxX - 7, 1, maxZ - 1), NORTH)
	}
}
