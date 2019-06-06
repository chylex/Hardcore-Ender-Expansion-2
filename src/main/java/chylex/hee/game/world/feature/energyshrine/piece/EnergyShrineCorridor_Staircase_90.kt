package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairBottomConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairTopConnection
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextFloat
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class EnergyShrineCorridor_Staircase_90(file: String) : EnergyShrineCorridor_Staircase(file){
	override val connections = arrayOf(
		EnergyShrineStairBottomConnection(Pos(maxX - 1, 0, maxZ), SOUTH),
		EnergyShrineStairTopConnection(Pos(0, maxY - 4, 2), WEST)
	)
	
	override fun nextRandomXZ(rand: Random, angle: Double): PosXZ{
		val distance = maxX - rand.nextFloat(0F, 3F)
		
		return PosXZ(
			(cos(angle) * distance).roundToInt(),
			(maxZ - sin(angle) * distance).roundToInt()
		)
	}
}
