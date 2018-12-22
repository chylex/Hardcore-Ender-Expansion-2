package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

object ParticleCorruptedEnergy : IParticleMaker{
	private val rand = Random()
	
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return ParticleTeleport.create(world, posX, posY, posZ, motX, motY, motZ, data).apply {
			if (rand.nextInt(3) == 0){
				setRBGColorF(redColorF * rand.nextFloat(0F, 0.2F), greenColorF * rand.nextFloat(0F, 0.2F), blueColorF * rand.nextFloat(0.1F, 0.3F))
			}
		}
	}
}
