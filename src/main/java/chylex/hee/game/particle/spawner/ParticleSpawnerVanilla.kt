package chylex.hee.game.particle.spawner
import chylex.hee.client.util.MC
import chylex.hee.game.particle.util.IOffset
import chylex.hee.game.particle.util.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.util.IOffset.None
import chylex.hee.game.particle.util.IShape
import net.minecraft.util.EnumParticleTypes
import java.util.Random

class ParticleSpawnerVanilla(
	type: EnumParticleTypes,
	private val pos: IOffset = None,
	private val mot: IOffset = None,
	private val ignoreRangeLimit: Boolean = false,
	hideOnMinimalSetting: Boolean = true
) : IParticleSpawner{
	private val particleID = type.particleID
	private val showSomeParticlesEvenOnMinimalSetting = !hideOnMinimalSetting
	
	private val tmpOffsetPos = MutableOffsetPoint()
	private val tmpOffsetMot = MutableOffsetPoint()
	
	override fun spawn(shape: IShape, rand: Random){
		val renderer = MC.instance.renderGlobal
		
		for(point in shape.points){
			pos.next(tmpOffsetPos, rand)
			mot.next(tmpOffsetMot, rand)
			
			renderer.spawnParticle(
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
