package chylex.hee.game.world.feature.basic.caves
import net.minecraft.util.math.Vec3d
import java.util.Random

interface ICavePather{
	fun nextOffset(rand: Random, point: Vec3d, stepSize: Double): Vec3d
}
