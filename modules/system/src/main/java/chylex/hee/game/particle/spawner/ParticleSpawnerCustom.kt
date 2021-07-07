package chylex.hee.game.particle.spawner

import chylex.hee.client.util.MC
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.ParticleSetting
import chylex.hee.game.fx.util.ParticleSetting.ALL
import chylex.hee.game.fx.util.ParticleSetting.DECREASED
import chylex.hee.game.fx.util.ParticleSetting.MINIMAL
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.spawner.properties.IOffset
import chylex.hee.game.particle.spawner.properties.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.spawner.properties.IOffset.None
import chylex.hee.game.particle.spawner.properties.IShape
import chylex.hee.util.math.square
import java.util.Random

class ParticleSpawnerCustom<D : IParticleData<T>, T>(
	private val type: IParticleMaker<T>,
	private val data: D? = null,
	private val pos: IOffset = None,
	private val mot: IOffset = None,
	private val skipTest: (Double, ParticleSetting, Random) -> Boolean,
) : IParticleSpawner {
	constructor(
		type: IParticleMaker<T>,
		data: D? = null,
		pos: IOffset = None,
		mot: IOffset = None,
		maxRange: Double = 32.0,
		hideOnMinimalSetting: Boolean = true,
	) : this(type, data, pos, mot, defaultSkipTest(maxRange, hideOnMinimalSetting))
	
	private val tmpOffsetPos = MutableOffsetPoint()
	private val tmpOffsetMot = MutableOffsetPoint()
	
	override fun spawn(shape: IShape, rand: Random) {
		val world = MC.world ?: return
		val playerPos = MC.instance.renderViewEntity?.posVec ?: return
		
		val particleManager = MC.particleManager
		val particleSetting = MC.particleSetting
		
		for (point in shape.points) {
			pos.next(tmpOffsetPos, rand)
			mot.next(tmpOffsetMot, rand)
			
			if (skipTest(playerPos.squareDistanceTo(point), particleSetting, rand)) {
				continue
			}
			
			type.create(
				world,
				point.x + tmpOffsetPos.x,
				point.y + tmpOffsetPos.y,
				point.z + tmpOffsetPos.z,
				tmpOffsetMot.x.toDouble(),
				tmpOffsetMot.y.toDouble(),
				tmpOffsetMot.z.toDouble(),
				data?.generate(rand)
			).let(particleManager::addEffect)
		}
	}
	
	private companion object {
		private fun defaultSkipTest(maxRange: Double, hideOnMinimalSetting: Boolean): (Double, ParticleSetting, Random) -> Boolean {
			val maxRangeSq = square(maxRange)
			val showSomeParticlesEvenOnMinimalSetting = !hideOnMinimalSetting
			
			fun shouldSkipParticle(particleSetting: ParticleSetting, rand: Random): Boolean {
				var adjustedSetting = particleSetting
				
				if (showSomeParticlesEvenOnMinimalSetting && adjustedSetting == MINIMAL && rand.nextInt(10) == 0) {
					adjustedSetting = DECREASED
				}
				
				if (adjustedSetting == DECREASED && rand.nextInt(3) == 0) {
					adjustedSetting = ALL
				}
				
				return adjustedSetting != ALL
			}
			
			return { distanceSq, particleSetting, rand -> distanceSq > maxRangeSq || shouldSkipParticle(particleSetting, rand) }
		}
	}
}
