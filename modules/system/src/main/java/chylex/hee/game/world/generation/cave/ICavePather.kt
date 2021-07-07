package chylex.hee.game.world.generation.cave

import net.minecraft.util.math.vector.Vector3d
import java.util.Random

interface ICavePather {
	fun nextOffset(rand: Random, point: Vector3d, stepSize: Double): Vector3d
}
