package chylex.hee.game.world.feature.basic.caves
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import net.minecraft.util.math.Vec3d

interface ICaveCarver{
	fun carve(world: SegmentedWorld, center: Vec3d, radius: Double, placer: IBlockPlacer): Boolean
}
