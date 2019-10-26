package chylex.hee.game.particle.data
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import java.util.Random

data class ParticleDataColorLifespanScale(val color: IntColor, val lifespan: Int, val scale: Float){
	class Generator(private val color: IRandomColor, private val lifespan: IntRange, private val scale: ClosedFloatingPointRange<Float>) : IParticleData<ParticleDataColorLifespanScale>{
		override fun generate(rand: Random): ParticleDataColorLifespanScale{
			return ParticleDataColorLifespanScale(color.next(rand), rand.nextInt(lifespan), rand.nextFloat(scale))
		}
	}
}
