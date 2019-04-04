package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairMiddleConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairTopConnection
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextFloat
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.SOUTH
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class EnergyShrineCorridor_Staircase_180_Top(file: String) : EnergyShrineCorridor_Staircase(file){
	override val connections = arrayOf(
		EnergyShrineStairTopConnection(Pos(2, size.maxY - 4, size.maxZ), SOUTH),
		EnergyShrineStairMiddleConnection(Pos(size.maxX, size.maxY, 0), EAST)
	)
	
	override fun nextRandomXZ(rand: Random, angle: Double): PosXZ{
		val distance = rand.nextFloat(0F, 3F)
		
		val distanceX = size.maxX - distance
		val distanceZ = size.maxZ - 0.5 - distance
		
		return PosXZ(
			(size.maxX - cos(angle) * distanceX).roundToInt(),
			(size.maxZ - sin(angle) * distanceZ).roundToInt()
		)
	}
}
