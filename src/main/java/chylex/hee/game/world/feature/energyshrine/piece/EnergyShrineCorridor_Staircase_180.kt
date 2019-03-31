package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairBottomConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairTopConnection
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.toRadians
import net.minecraft.util.EnumFacing.SOUTH
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class EnergyShrineCorridor_Staircase_180(file: String) : EnergyShrineCorridor_Staircase(file){
	override val connections = arrayOf(
		EnergyShrineStairBottomConnection(Pos(size.maxX - 1, 0, size.maxZ), SOUTH),
		EnergyShrineStairTopConnection(Pos(2, size.maxY - 4, size.maxZ), SOUTH)
	)
	
	override fun nextRandomXZ(rand: Random, progress: Float): Pair<Int, Int>{
		val angle = ((progress * 175F) + rand.nextFloat(0F, 5F)).toRadians()
		val distance = (size.maxX * 0.5F) - rand.nextFloat(0F, 3F)
		
		return Pair(
			(size.centerX + cos(angle) * distance).roundToInt(),
			(size.maxZ - 1 - sin(angle) * distance).roundToInt() // offset by -1 since it's not a perfect circle, but good enough
		)
	}
}
