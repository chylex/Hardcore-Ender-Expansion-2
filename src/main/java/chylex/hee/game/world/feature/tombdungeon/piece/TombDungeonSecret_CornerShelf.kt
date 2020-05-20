package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.SECRET_CONNECTOR
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import java.util.Random

class TombDungeonSecret_CornerShelf(file: String) : TombDungeonSecret(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(SECRET_CONNECTOR, Pos(2, 0, maxZ), Facing.SOUTH)
	)
	
	override fun pickRandomEntrancePoint(rand: Random): BlockPos{
		return Pos(
			rand.nextInt(rand.nextInt(0, 1), rand.nextInt(3, 4)),
			rand.nextInt(1, maxY),
			maxZ
		)
	}
	
	override fun pickChestPosition(rand: Random): Pair<BlockPos, Direction>?{
		return Pos(maxX - 1, 2, 2) to WEST
	}
}
