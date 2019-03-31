package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairBottomConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineStairTopConnection
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.toRadians
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class EnergyShrineCorridor_Staircase_90(file: String) : EnergyShrineCorridor_Staircase(file){
	override val connections = arrayOf(
		EnergyShrineStairBottomConnection(Pos(size.maxX - 1, 0, size.maxZ), SOUTH),
		EnergyShrineStairTopConnection(Pos(0, size.maxY - 4, 2), WEST)
	)
	
	override fun nextRandomXZ(rand: Random, progress: Float): Pair<Int, Int>{
		val angle = ((progress * 85F) + rand.nextFloat(0F, 5F)).toRadians()
		val distance = size.maxX - rand.nextFloat(0F, 3F)
		
		return Pair(
			(cos(angle) * distance).roundToInt(),
			(size.maxZ - sin(angle) * distance).roundToInt()
		)
	}
}
