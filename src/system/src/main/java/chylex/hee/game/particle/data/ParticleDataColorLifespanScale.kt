package chylex.hee.game.particle.data

import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.color.IntColor
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import java.util.Random

data class ParticleDataColorLifespanScale(val color: IntColor, val lifespan: Int, val scale: Float) {
	class Generator(private val color: IRandomColor, private val lifespan: IntRange, private val scale: ClosedFloatingPointRange<Float>) : IParticleData<ParticleDataColorLifespanScale> {
		override fun generate(rand: Random): ParticleDataColorLifespanScale {
			return ParticleDataColorLifespanScale(color.next(rand), rand.nextInt(lifespan), rand.nextFloat(scale))
		}
	}
	
	abstract class ParticleMaker : IParticleMaker.WithData<ParticleDataColorLifespanScale>() {
		abstract val defaultColor: IRandomColor
		abstract val defaultLifespan: IntRange
		abstract val defaultScale: ClosedFloatingPointRange<Float>
		
		protected val ParticleDataColorLifespanScale?.orDefault
			get() = this ?: ParticleDataColorLifespanScale(defaultColor.next(rand), rand.nextInt(defaultLifespan), rand.nextFloat(defaultScale))
		
		fun Data(
			color: IRandomColor = defaultColor,
			lifespan: IntRange = defaultLifespan,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(color, lifespan, scale)
		
		fun Data(
			color: IntColor,
			lifespan: IntRange = defaultLifespan,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(IRandomColor.Static(color), lifespan, scale)
		
		fun Data(
			color: IRandomColor = defaultColor,
			lifespan: Int,
			scale: ClosedFloatingPointRange<Float> = defaultScale,
		) = Generator(color, lifespan..lifespan, scale)
		
		fun Data(
			color: IRandomColor = defaultColor,
			lifespan: IntRange = defaultLifespan,
			scale: Float,
		) = Generator(color, lifespan, scale..scale)
		
		fun Data(
			color: IRandomColor = defaultColor,
			lifespan: Int,
			scale: Float,
		) = Generator(color, lifespan..lifespan, scale..scale)
		
		fun Data(
			color: IntColor,
			lifespan: IntRange = defaultLifespan,
			scale: Float
		) = Generator(IRandomColor.Static(color), lifespan, scale..scale)
		
		fun Data(
			color: IntColor,
			lifespan: Int,
			scale: ClosedFloatingPointRange<Float> = defaultScale
		) = Generator(IRandomColor.Static(color), lifespan..lifespan, scale)
		
		fun Data(
			color: IntColor,
			lifespan: Int,
			scale: Float,
		) = Generator(IRandomColor.Static(color), lifespan..lifespan, scale..scale)
	}
}
