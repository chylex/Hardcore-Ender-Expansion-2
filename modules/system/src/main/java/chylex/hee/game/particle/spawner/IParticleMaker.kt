package chylex.hee.game.particle.spawner

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import java.util.Random

interface IParticleMaker<T> {
	@Sided(Side.CLIENT)
	fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: T?): Particle
	
	private companion object {
		val rand = Random()
	}
	
	abstract class WithData<T> : IParticleMaker<T> {
		protected val rand
			get() = IParticleMaker.rand
		
		lateinit var sprite: IAnimatedSprite
	}
	
	abstract class Simple : WithData<Unit>() {
		@Sided(Side.CLIENT)
		final override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Unit?): Particle {
			return create(world, posX, posY, posZ, motX, motY, motZ)
		}
		
		@Sided(Side.CLIENT)
		abstract fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle
	}
}
