package chylex.hee.game.world.generation.cave

import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import net.minecraft.util.math.vector.Vector3d

interface ICaveCarver {
	fun carve(world: SegmentedWorld, center: Vector3d, radius: Double, placer: IBlockPlacer): Boolean
}
