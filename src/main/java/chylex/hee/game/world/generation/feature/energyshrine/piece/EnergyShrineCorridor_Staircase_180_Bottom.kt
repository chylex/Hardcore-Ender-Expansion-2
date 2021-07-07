package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.STAIR_BOTTOM
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.STAIR_MIDDLE
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.system.random.nextFloat
import chylex.hee.util.math.Pos
import chylex.hee.util.math.PosXZ
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST
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
