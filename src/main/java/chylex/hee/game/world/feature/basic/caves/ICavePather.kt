package chylex.hee.game.world.feature.basic.caves
import net.minecraft.util.math.Vec3d
import java.util.Random

interface ICavePather{
	fun nextOffset(rand: Random, point: Vec3d): Vec3d // TODO kinda messy with normalization in multiple places
}
