package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem

class TombDungeonRoom_Main_Pillars(file: String, isFancy: Boolean) : TombDungeonRoom(file, isFancy){
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX,      0, 0), NORTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX - 14, 0, 0), NORTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX + 14, 0, 0), NORTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX,      0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX - 14, 0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX + 14, 0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX, 0, centerZ     ), EAST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX, 0, centerZ - 14), EAST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX, 0, centerZ + 14), EAST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(0, 0, centerZ     ), WEST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(0, 0, centerZ - 14), WEST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(0, 0, centerZ + 14), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		repeat(rand.nextInt(2, 3)){
			val chestPos = Pos(
				8 + (7 * rand.nextInt(0, 5)) + rand.nextInt(0, 1),
				1,
				8 + (7 * rand.nextInt(0, 5)) + rand.nextInt(0, 1)
			)
			
			if (world.getBlock(chestPos) === ModBlocks.DUSTY_STONE_BRICKS){
				world.setBlock(chestPos.up(), ModBlocks.DUSTY_STONE_CRACKED)
				placeChest(world, chestPos, rand.nextItem(Facing4))
			}
		}
	}
}
