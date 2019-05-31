package chylex.hee.game.particle.spawner.factory
import org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY
import java.util.Random

interface IParticleData{
	fun generate(rand: Random): IntArray
	
	object Empty : IParticleData{
		override fun generate(rand: Random): IntArray = EMPTY_INT_ARRAY
	}
	
	open class Static(private val data: IntArray) : IParticleData{
		override fun generate(rand: Random) = data
		
		/**
		 * Performs validation of [inputData], using itself as the fallback value.
		 *
		 * The intended use is creating an instance with valid default values, and calling [validate] with the [IntArray] provided to [IParticleMaker.create].
		 * By default, the returned array is guaranteed to have at least as many elements as the internal [data] array, removing the need to check bounds.
		 */
		open fun validate(inputData: IntArray): IntArray{
			return if (inputData.size < data.size)
				data
			else
				inputData
		}
	}
}
