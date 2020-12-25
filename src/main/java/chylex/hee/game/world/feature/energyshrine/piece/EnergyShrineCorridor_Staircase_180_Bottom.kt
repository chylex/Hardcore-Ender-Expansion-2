package chylex.hee.game.world.feature.energyshrine.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.STAIR_BOTTOM
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.STAIR_MIDDLE
import chylex.hee.game.world.math.PosXZ
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextFloat
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class EnergyShrineCorridor_Staircase_180_Bottom(file: String) : EnergyShrineCorridor_Staircase(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(STAIR_MIDDLE, Pos(0, maxY, 0), WEST),
		EnergyShrineConnection(STAIR_BOTTOM, Pos(maxX - 1, 0, maxZ), SOUTH)
	)
	
	override fun nextRandomXZ(rand: Random, angle: Double): PosXZ {
		val distance = rand.nextFloat(0F, 3F)
		
		val distanceX = maxX - distance
		val distanceZ = maxZ - 0.5 - distance
		
		return PosXZ(
			(cos(angle) * distanceX).roundToInt(),
			(maxZ - sin(angle) * distanceZ).roundToInt()
		)
	}
}
