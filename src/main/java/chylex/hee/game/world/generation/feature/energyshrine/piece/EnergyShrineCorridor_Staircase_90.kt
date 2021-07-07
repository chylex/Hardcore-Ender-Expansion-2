package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.STAIR_BOTTOM
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.STAIR_TOP
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

class EnergyShrineCorridor_Staircase_90(file: String) : EnergyShrineCorridor_Staircase(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(STAIR_BOTTOM, Pos(maxX - 1, 0, maxZ), SOUTH),
		EnergyShrineConnection(STAIR_TOP, Pos(0, maxY - 4, 2), WEST)
	)
	
	override fun nextRandomXZ(rand: Random, angle: Double): PosXZ {
		val distance = maxX - rand.nextFloat(0F, 3F)
		
		return PosXZ(
			(cos(angle) * distance).roundToInt(),
			(maxZ - sin(angle) * distance).roundToInt()
		)
	}
}
