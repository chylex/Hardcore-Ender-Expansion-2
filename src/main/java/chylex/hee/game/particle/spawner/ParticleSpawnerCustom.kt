package chylex.hee.game.particle.spawner
import chylex.hee.game.particle.spawner.IParticleSpawner.Companion.mc
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleData.Empty
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.IOffset
import chylex.hee.game.particle.util.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.util.IOffset.None
import chylex.hee.game.particle.util.IShape
import chylex.hee.game.particle.util.ParticleSetting
import chylex.hee.game.particle.util.ParticleSetting.ALL
import chylex.hee.game.particle.util.ParticleSetting.DECREASED
import chylex.hee.game.particle.util.ParticleSetting.MINIMAL
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import java.util.Random

class ParticleSpawnerCustom(
	private val type: IParticleMaker,
	private val data: IParticleData = Empty,
	private val pos: IOffset = None,
	private val mot: IOffset = None,
	private val skipTest: (Double, ParticleSetting, Random) -> Boolean
) : IParticleSpawner{
	constructor(
		type: IParticleMaker,
		data: IParticleData = Empty,
		pos: IOffset = None,
		mot: IOffset = None,
		maxRange: Double = 32.0,
		hideOnMinimalSetting: Boolean = true
	) : this(type, data, pos, mot, defaultSkipTest(maxRange, hideOnMinimalSetting))
	
	private val tmpOffsetPos = MutableOffsetPoint()
	private val tmpOffsetMot = MutableOffsetPoint()
	
	override fun spawn(shape: IShape, rand: Random){
		val mc = mc
		
		val world = mc.world ?: return
		val playerPos = mc.renderViewEntity?.posVec ?: return
		
		val particleManager = mc.effectRenderer ?: return
		val particleSetting = ParticleSetting.get(mc.gameSettings)
		
		for(point in shape.points){
			pos.next(tmpOffsetPos, rand)
			mot.next(tmpOffsetMot, rand)
			
			if (skipTest(playerPos.squareDistanceTo(point), particleSetting, rand)){
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
				data.generate(rand)
			).let(particleManager::addEffect)
		}
	}
	
	private companion object{
		private fun defaultSkipTest(maxRange: Double, hideOnMinimalSetting: Boolean): (Double, ParticleSetting, Random) -> Boolean{
			val maxRangeSq = square(maxRange)
			val showSomeParticlesEvenOnMinimalSetting = !hideOnMinimalSetting
			
			fun shouldSkipParticle(particleSetting: ParticleSetting, rand: Random): Boolean{
				var adjustedSetting = particleSetting
				
				if (showSomeParticlesEvenOnMinimalSetting && adjustedSetting == MINIMAL && rand.nextInt(10) == 0){
					adjustedSetting = DECREASED
				}
				
				if (adjustedSetting == DECREASED && rand.nextInt(3) == 0){
					adjustedSetting = ALL
				}
				
				return adjustedSetting != ALL
			}
			
			return { distanceSq, particleSetting, rand -> distanceSq > maxRangeSq || shouldSkipParticle(particleSetting, rand) }
		}
	}
}