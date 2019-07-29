package chylex.hee.game.world.feature.basic.caves.impl
import chylex.hee.game.world.feature.basic.caves.ICaveCarver
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.square
import net.minecraft.util.math.Vec3d

class CaveCarverSphere(private val maxRandomRadiusReduction: Float) : ICaveCarver{
	override fun carve(world: SegmentedWorld, center: Vec3d, radius: Double, placer: IBlockPlacer): Boolean{
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
