package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.floorToInt
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleSmokeNormal
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ParticleSmokeCustom : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		scale: Float = 1F
	) : IParticleData.Static(intArrayOf(
		(scale * 100F).floorToInt()
	))
	
	private val DEFAULT_DATA = Data()
	
	@SideOnly(Side.CLIENT)
	private class Instance(
		world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray
	) : ParticleSmokeNormal(
		world, posX, posY, posZ, motX, motY, motZ, DEFAULT_DATA.validate(unsafeData)[0] * 0.01F
	)
}
