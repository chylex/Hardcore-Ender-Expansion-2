package chylex.hee.game.particle.data
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.nextFloat
import java.util.Random

data class ParticleDataColorScale(val color: IntColor, val scale: Float){
	class Generator(private val color: IRandomColor, private val scale: ClosedFloatingPointRange<Float>) : IParticleData<ParticleDataColorScale>{
		override fun generate(rand: Random): ParticleDataColorScale{
			return ParticleDataColorScale(color.next(rand), rand.nextFloat(scale))
		}
	}
}
