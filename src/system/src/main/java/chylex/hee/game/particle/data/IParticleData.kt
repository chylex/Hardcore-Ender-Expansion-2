package chylex.hee.game.particle.data

import java.util.Random

interface IParticleData<T> {
	fun generate(rand: Random): T
	
	open class Self<T> : IParticleData<T> {
		@Suppress("UNCHECKED_CAST")
		override fun generate(rand: Random) = this as T
	}
}
