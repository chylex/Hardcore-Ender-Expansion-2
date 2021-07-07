package chylex.hee.game.particle.data

import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.random.nextFloat
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.IntColor
import java.util.Random

data class ParticleDataColorScale(val color: IntColor, val scale: Float) {
	class Generator(private val color: IColorGenerator, private val scale: ClosedFloatingPointRange<Float>) : IParticleData<ParticleDataColorScale> {
		override fun generate(rand: Random): ParticleDataColorScale {
			return ParticleDataColorScale(color.next(rand), rand.nextFloat(scale))
		}
	}
	
	abstract class ParticleMaker : IParticleMaker.WithData<ParticleDataColorScale>() {
		abstract val defaultColor: IColorGenerator
		abstract val defaultScale: ClosedFloatingPointRange<Float>
		
		protected val ParticleDataColorScale?.orDefault
			get() = this ?: ParticleDataColorScale(defaultColor.next(rand), rand.nextFloat(defaultScale))
		
		fun Data(
			color: IColorGenerator = defaultColor,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(color, scale)
		
		fun Data(
			color: IColorGenerator = defaultColor,
			scale: Float,
		) = Generator(color, scale..scale)
	}
}
