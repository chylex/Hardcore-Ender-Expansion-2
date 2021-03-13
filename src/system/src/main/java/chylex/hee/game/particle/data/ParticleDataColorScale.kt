package chylex.hee.game.particle.data

import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.color.IntColor
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.nextFloat
import java.util.Random

data class ParticleDataColorScale(val color: IntColor, val scale: Float) {
	class Generator(private val color: IRandomColor, private val scale: ClosedFloatingPointRange<Float>) : IParticleData<ParticleDataColorScale> {
		override fun generate(rand: Random): ParticleDataColorScale {
			return ParticleDataColorScale(color.next(rand), rand.nextFloat(scale))
		}
	}
	
	abstract class ParticleMaker : IParticleMaker.WithData<ParticleDataColorScale>() {
		abstract val defaultColor: IRandomColor
		abstract val defaultScale: ClosedFloatingPointRange<Float>
		
		protected val ParticleDataColorScale?.orDefault
			get() = this ?: ParticleDataColorScale(defaultColor.next(rand), rand.nextFloat(defaultScale))
		
		fun Data(
			color: IRandomColor = defaultColor,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(color, scale)
		
		fun Data(
			color: IntColor,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(IRandomColor.Static(color), scale)
		
		fun Data(
			color: IRandomColor = defaultColor,
			scale: Float,
		) = Generator(color, scale..scale)
		
		fun Data(
			color: IntColor,
			scale: Float,
		) = Generator(IRandomColor.Static(color), scale..scale)
	}
}
