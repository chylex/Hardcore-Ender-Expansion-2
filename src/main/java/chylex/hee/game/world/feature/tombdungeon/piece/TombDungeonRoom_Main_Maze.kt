package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.PosXZ
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import net.minecraft.util.math.BlockPos

class TombDungeonRoom_Main_Maze(file: String, isFancy: Boolean) : TombDungeonRoom(file, isFancy){
	override val secretAttachWeight = 3
	
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX - 2, 0, 0), NORTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX - 10, 0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(14, 0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX, 0, 10), EAST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(maxX, 0, maxZ - 2), EAST),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(0, 0, 6), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		val brick = Single(ModBlocks.DUSTY_STONE_BRICKS)
		val top = maxY - 1
		
		placeCrumblingCeiling(world, instance, rand.nextInt(0, 3))
		
		if (rand.nextBoolean()){
			world.placeWalls(Pos(1, 1, 4), Pos(3, top, 4), brick)
			world.placeWalls(Pos(5, 1, 8), Pos(7, top, 8), Air)
		}
		
		if (rand.nextBoolean()){
			world.placeWalls(Pos(12, 1, maxZ - 3), Pos(12, top, maxZ - 1), brick)
			world.placeWalls(Pos(4, 1, maxZ - 11), Pos(4, top, maxZ - 9), Air)
		}
		
		if (rand.nextBoolean()){
			world.placeWalls(Pos(1, 1, 16), Pos(3, top, 16), brick)
			world.placeWalls(Pos(16, 1, 21), Pos(16, top, 23), brick)
			world.placeWalls(Pos(10, 1, 20), Pos(12, top, 20), Air)
		}
		
		if (rand.nextBoolean()){
			world.placeWalls(Pos(maxX - 3, 1, 4), Pos(maxX - 1, top, 4), brick)
		}
		
		if (rand.nextBoolean()){
			world.placeWalls(Pos(maxX - 7, 1, maxZ - 4), Pos(maxX - 5, top, maxZ - 4), brick)
			world.placeWalls(Pos(maxX - 12, 1, maxZ - 3), Pos(maxX - 12, top, maxZ - 1), Air)
		}
		
		val chestPositions = listOf(
			PosXZ(2, 2),
			PosXZ(18, 22),
			PosXZ(10, 14),
			PosXZ(10, 26),
			PosXZ(10, maxZ - 2),
			PosXZ(maxX - 2, 6),
			PosXZ(maxX - 2, 19)
		)
		
		repeat(rand.nextInt(2, 3)){
			for(attempt in 1..5){
				if (tryPlaceChest(world, rand.nextItem(chestPositions).withY(1))){
					break
				}
			}
		}
	}
	
	private fun tryPlaceChest(world: IStructureWorld, pos: BlockPos): Boolean{
		if (!world.isAir(pos)){
			return false
		}
		
		val facing = Facing4.singleOrNull { world.isAir(pos.offset(it, 2)) } ?: return false
		
		placeChest(world, pos, facing)
		return true
	}
}
