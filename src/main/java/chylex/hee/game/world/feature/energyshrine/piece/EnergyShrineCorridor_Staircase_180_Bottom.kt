package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairBottomConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairMiddleConnection
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextFloat
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class EnergyShrineCorridor_Staircase_180_Bottom(file: String) : EnergyShrineCorridor_Staircase(file){
	override val connections = arrayOf(
		EnergyShrineStairMiddleConnection(Pos(0, maxY, 0), WEST),
		EnergyShrineStairBottomConnection(Pos(maxX - 1, 0, maxZ), SOUTH)
	)
	
	override fun nextRandomXZ(rand: Random, angle: Double): PosXZ{
		val distance = rand.nextFloat(0F, 3F)
		
		val distanceX = maxX - distance
		val distanceZ = maxZ - 0.5 - distance
		
		return PosXZ(
			(cos(angle) * distanceX).roundToInt(),
			(maxZ - sin(angle) * distanceZ).roundToInt()
		)
	}
}
