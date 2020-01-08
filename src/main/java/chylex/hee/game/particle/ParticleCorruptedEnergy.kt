package chylex.hee.game.particle
import chylex.hee.game.particle.base.ParticleBase
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import java.util.Random

object ParticleCorruptedEnergy : IParticleMaker<ParticleDataColorLifespanScale>{
	private val rand = Random()
	
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle{
		return ParticleTeleport.create(world, posX, posY, posZ, motX, motY, motZ, data).apply {
			if (rand.nextInt(3) == 0){
				val me = this as ParticleBase // UPDATE ugly
				setColor(me.redF * rand.nextFloat(0F, 0.2F), me.greenF * rand.nextFloat(0F, 0.2F), me.blueF * rand.nextFloat(0.1F, 0.3F))
			}
		}
	}
}
