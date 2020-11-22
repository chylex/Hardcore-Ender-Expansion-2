package chylex.hee.game.particle.spawner
import chylex.hee.HEE
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import net.minecraft.particles.BasicParticleType
import net.minecraft.particles.ParticleType

interface IParticleMaker<T>{
	@Sided(Side.CLIENT)
	fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: T?): Particle
	
	abstract class WithData<T> : IParticleMaker<T>{
		protected lateinit var sprite: IAnimatedSprite
		
		val makeType: ParticleType<*>
			get(){
				val type = BasicParticleType(false)
				
				HEE.proxy.registerParticle(type, this){
					sprite = it
				}
				
				return type
			}
	}
	
	abstract class Simple : WithData<Unit>(){
		@Sided(Side.CLIENT)
		final override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Unit?): Particle{
			return create(world, posX, posY, posZ, motX, motY, motZ)
		}
		
		@Sided(Side.CLIENT)
		abstract fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle
	}
}
