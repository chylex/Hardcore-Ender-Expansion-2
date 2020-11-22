package chylex.hee.game.particle
import chylex.hee.game.particle.ParticleFlameCustom.Data
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.particle.FlameParticle
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld

object ParticleFlameCustom : IParticleMaker.WithData<Data>(){
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		val maxAge: Int
	) : IParticleData.Self<Data>()
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?) : FlameParticle(world, posX, posY, posZ, motX, motY, motZ){
		init{
			selectSpriteRandomly(ParticleFlameCustom.sprite)
			maxAge = data?.maxAge ?: 0
		}
	}
}
