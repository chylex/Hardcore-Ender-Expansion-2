package chylex.hee.game.particle.spawner

import chylex.hee.client.MC
import chylex.hee.game.particle.spawner.properties.IOffset
import chylex.hee.game.particle.spawner.properties.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.spawner.properties.IOffset.None
import chylex.hee.game.particle.spawner.properties.IShape
import net.minecraft.particles.IParticleData
import java.util.Random

class ParticleSpawnerVanilla(
	type: IParticleData,
	private val pos: IOffset = None,
	private val mot: IOffset = None,
	private val ignoreRangeLimit: Boolean = false,
	hideOnMinimalSetting: Boolean = true,
) : IParticleSpawner {
	private val particleID = type
	private val showSomeParticlesEvenOnMinimalSetting = !hideOnMinimalSetting
	
	private val tmpOffsetPos = MutableOffsetPoint()
	private val tmpOffsetMot = MutableOffsetPoint()
	
	override fun spawn(shape: IShape, rand: Random) {
		val renderer = MC.instance.worldRenderer
		
		for(point in shape.points) {
			pos.next(tmpOffsetPos, rand)
			mot.next(tmpOffsetMot, rand)
			
			renderer.addParticle(
				particleID,
				ignoreRangeLimit,
				showSomeParticlesEvenOnMinimalSetting,
				point.x + tmpOffsetPos.x,
				point.y + tmpOffsetPos.y,
				point.z + tmpOffsetPos.z,
				tmpOffsetMot.x.toDouble(),
				tmpOffsetMot.y.toDouble(),
				tmpOffsetMot.z.toDouble()
			)
		}
	}
}
