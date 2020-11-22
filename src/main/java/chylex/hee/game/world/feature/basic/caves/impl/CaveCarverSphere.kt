package chylex.hee.game.world.feature.basic.caves.impl
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInCenteredBoxMutable
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.feature.basic.caves.ICaveCarver
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.math.square
import chylex.hee.system.random.nextFloat
import net.minecraft.util.math.vector.Vector3d

class CaveCarverSphere(private val maxRandomRadiusReduction: Float) : ICaveCarver{
	override fun carve(world: SegmentedWorld, center: Vector3d, radius: Double, placer: IBlockPlacer): Boolean{
		val rand = world.rand
		
		val carveCenter = Pos(center)
		var anySuccess = false
		
		for(pos in carveCenter.allInCenteredBoxMutable(radius, radius, radius)){
			if (pos.distanceSqTo(carveCenter) <= square(radius - rand.nextFloat(0F, maxRandomRadiusReduction)) && placer.place(world, pos)){
				anySuccess = true
			}
		}
		
		return anySuccess
	}
}
