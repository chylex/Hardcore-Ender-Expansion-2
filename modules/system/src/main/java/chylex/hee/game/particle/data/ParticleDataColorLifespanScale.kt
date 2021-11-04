package chylex.hee.game.particle.data

import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.IntColor
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import java.util.Random

data class ParticleDataColorLifespanScale(val color: IntColor, val lifespan: Int, val scale: Float) {
	class Generator(private val color: IColorGenerator, private val lifespan: IntRange, private val scale: ClosedFloatingPointRange<Float>) : IParticleData<ParticleDataColorLifespanScale> {
		override fun generate(rand: Random): ParticleDataColorLifespanScale {
			return ParticleDataColorLifespanScale(color.next(rand), rand.nextInt(lifespan), rand.nextFloat(scale))
		}
	}
	
	abstract class ParticleMaker : IParticleMaker.WithData<ParticleDataColorLifespanScale>() {
		abstract val defaultColor: IColorGenerator
		abstract val defaultLifespan: IntRange
		abstract val defaultScale: ClosedFloatingPointRange<Float>
		
		protected val ParticleDataColorLifespanScale?.orDefault
			get() = this ?: ParticleDataColorLifespanScale(defaultColor.next(rand), rand.nextInt(defaultLifespan), rand.nextFloat(defaultScale))
		
		fun Data(
			color: IColorGenerator = defaultColor,
			lifespan: IntRange = defaultLifespan,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(color, lifespan, scale)
		
		fun Data(
			color: IColorGenerator = defaultColor,
			lifespan: Int,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(color, lifespan..lifespan, scale)
		
		fun Data(
			color: IColorGenerator = defaultColor,
			lifespan: IntRange = defaultLifespan,
			scale: Float,
		) = Generator(color, lifespan, scale..scale)
		
		fun Data(
			color: IColorGenerator = defaultColor,
			lifespan: Int,
			scale: Float,
		) = Generator(color, lifespan..lifespan, scale..scale)
	}
}
