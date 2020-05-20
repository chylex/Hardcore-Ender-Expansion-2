package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.SECRET_CONNECTOR
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import net.minecraft.util.math.BlockPos
import java.util.Random

class TombDungeonSecret_Rubble(file: String) : TombDungeonSecret(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(SECRET_CONNECTOR, Pos(centerX, 0, maxZ), SOUTH)
	)
	
	override fun pickRandomEntrancePoint(rand: Random): BlockPos{
		return Pos(
			rand.nextInt(rand.nextInt(centerX - 2, centerX - 1), rand.nextInt(centerX + 1, centerX + 2)),
			rand.nextInt(1, maxY - 2),
			maxZ
		)
	}
	
	override fun pickChestPosition(rand: Random) = when(rand.nextInt(0, 2)){
		0    -> Pos(1, 2, maxZ - 3) to EAST
		1    -> Pos(centerX, 2, maxZ - 4) to SOUTH
		else -> Pos(maxX - 1, 2, maxZ - 2) to WEST
	}
}
