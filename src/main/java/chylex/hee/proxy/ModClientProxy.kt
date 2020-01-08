package chylex.hee.proxy
import chylex.hee.client.util.MC
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.init.ModParticles
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.client.particle.ParticleManager
import net.minecraft.particles.BasicParticleType

@Suppress("unused", "RemoveExplicitTypeArguments")
@Sided(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer() = MC.player
	
	// Particles
	
	override fun registerParticle(type: BasicParticleType, maker: IParticleMaker<*>, callback: (IAnimatedSprite) -> Unit){
		ModParticles.Client.addFactory(type, maker, callback)
	}
	
	private var prevParticleManager: ParticleManager? = null
	
	override fun pauseParticles() = with(MC.instance){
		if (particles != null){
			prevParticleManager = particles
			particles = null
		}
	}
	
	override fun resumeParticles() = with(MC.instance){
		if (particles == null){
			particles = prevParticleManager
			prevParticleManager = null
		}
	}
}
