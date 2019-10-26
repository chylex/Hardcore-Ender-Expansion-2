package chylex.hee.game.particle
import chylex.hee.game.particle.ParticleFlameCustom.Data
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFlame
import net.minecraft.world.World

object ParticleFlameCustom : IParticleMaker<Data>{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		val maxAge: Int
	) : IParticleData.Self<Data>()
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?) : ParticleFlame(world, posX, posY, posZ, motX, motY, motZ){
		init{
			particleMaxAge = data?.maxAge ?: 0
		}
	}
}
