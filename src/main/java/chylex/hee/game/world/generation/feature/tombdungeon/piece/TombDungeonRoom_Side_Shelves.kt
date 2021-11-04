package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextRounded
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class TombDungeonRoom_Side_Shelves(file: String, isFancy: Boolean) : TombDungeonRoom(file, isFancy) {
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, maxZ), SOUTH),
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		repeat(rand.nextRounded(1.4F)) {
			val chestPos = Pos(
				if (rand.nextBoolean()) 1 else maxX - 1,
				if (rand.nextBoolean()) 2 else 4,
				2 + (5 * rand.nextInt(0, 2) + rand.nextInt(0, 3))
			)
			
			if (world.isAir(chestPos)) {
				placeChest(world, instance, chestPos, if (chestPos.x < centerX) EAST else WEST)
			}
		}
	}
}
