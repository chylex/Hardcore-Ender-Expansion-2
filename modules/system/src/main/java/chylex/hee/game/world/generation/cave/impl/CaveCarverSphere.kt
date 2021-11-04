package chylex.hee.game.world.generation.cave.impl

import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.cave.ICaveCarver
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.game.world.util.allInCenteredBoxMutable
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.util.math.Pos
import chylex.hee.util.math.square
import chylex.hee.util.random.nextFloat
import net.minecraft.util.math.vector.Vector3d

class CaveCarverSphere(private val maxRandomRadiusReduction: Float) : ICaveCarver {
	override fun carve(world: SegmentedWorld, center: Vector3d, radius: Double, placer: IBlockPlacer): Boolean {
		val rand = world.rand
		
		val carveCenter = Pos(center)
		var anySuccess = false
		
		for (pos in carveCenter.allInCenteredBoxMutable(radius, radius, radius)) {
			if (pos.distanceSqTo(carveCenter) <= square(radius - rand.nextFloat(0F, maxRandomRadiusReduction)) && placer.place(world, pos)) {
				anySuccess = true
			}
		}
		
		return anySuccess
	}
}
