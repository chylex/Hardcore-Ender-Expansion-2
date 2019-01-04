package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFlame
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ParticleFlameCustom : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		maxAge: Int = 0
	) : IParticleData.Static(intArrayOf(
		maxAge
	))
	
	private val DEFAULT_DATA = Data()
	
	@SideOnly(Side.CLIENT)
	private class Instance(
		world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray
	) : ParticleFlame(
		world, posX, posY, posZ, motX, motY, motZ
	){
		init{
			particleMaxAge = DEFAULT_DATA.validate(unsafeData)[0]
		}
	}
}
