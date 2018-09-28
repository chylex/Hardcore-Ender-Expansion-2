package chylex.hee.game.particle.spawner
import chylex.hee.game.particle.spawner.IParticleSpawner.Companion.mc
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleData.Empty
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.IOffset
import chylex.hee.game.particle.util.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.util.IOffset.None
import chylex.hee.game.particle.util.IShape
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import java.util.Random

class ParticleSpawnerCustom(
	private val type: IParticleMaker,
	private val data: IParticleData = Empty,
	private val pos: IOffset = None,
	private val mot: IOffset = None,
	maxRange: Double = 32.0,
	hideOnMinimalSetting: Boolean = true
) : IParticleSpawner{
	private val maxRangeSq = square(maxRange)
	private val showSomeParticlesEvenOnMinimalSetting = !hideOnMinimalSetting
	
	private val tmpOffsetPos = MutableOffsetPoint()
	private val tmpOffsetMot = MutableOffsetPoint()
	
	override fun spawn(shape: IShape, rand: Random){
		val mc = mc
		
		val world = mc.world ?: return
		val playerPos = mc.renderViewEntity?.posVec ?: return
		
		val particleManager = mc.effectRenderer ?: return
		val particleSetting = mc.gameSettings.particleSetting
		
		for(point in shape.points){
			pos.next(tmpOffsetPos, rand)
			mot.next(tmpOffsetMot, rand)
			
			if (shouldSkipParticle(particleSetting, rand) || playerPos.squareDistanceTo(point.x, point.y, point.z) > maxRangeSq){
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
	
	private fun shouldSkipParticle(particleSetting: Int, rand: Random): Boolean{
		var adjustedSetting = particleSetting
		
		if (showSomeParticlesEvenOnMinimalSetting && adjustedSetting == 2 && rand.nextInt(10) == 0){
			adjustedSetting = 1
		}
		
		if (adjustedSetting == 1 && rand.nextInt(3) == 0){
			adjustedSetting = 2
		}
		
		return adjustedSetting > 1
	}
}
