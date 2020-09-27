package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST

class TombDungeonRoom_Main_CrossroadRooms(file: String, isFancy: Boolean) : TombDungeonRoom(file, isFancy){
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, 0), NORTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX, 0, centerZ), EAST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		placeChest(world, instance, Pos(1, maxY - 2, 9), EAST)
	}
}
