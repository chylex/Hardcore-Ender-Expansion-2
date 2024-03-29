package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Facing4
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class TombDungeonRoom_Main_Pillars(file: String, isFancy: Boolean) : TombDungeonRoom(file, isFancy) {
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
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		repeat(rand.nextInt(2, 3)) {
			val chestPos = Pos(
				8 + (7 * rand.nextInt(0, 5)) + rand.nextInt(0, 1),
				1,
				8 + (7 * rand.nextInt(0, 5)) + rand.nextInt(0, 1)
			)
			
			if (world.getBlock(chestPos) === ModBlocks.DUSTY_STONE_BRICKS) {
				world.setBlock(chestPos.up(), if (it == 1) ModBlocks.DUSTY_STONE_CRACKED else ModBlocks.DUSTY_STONE_DAMAGED)
				placeChest(world, instance, chestPos, rand.nextItem(Facing4))
			}
		}
	}
}
