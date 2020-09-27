package chylex.hee.proxy
import chylex.hee.client.MC
import chylex.hee.client.VanillaResourceOverrides
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.init.ModParticles
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.particles.BasicParticleType

@Suppress("unused", "RemoveExplicitTypeArguments")
@Sided(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	init{
		VanillaResourceOverrides.register()
	}
	
	override fun getClientSidePlayer() = MC.player
	
	override fun registerParticle(type: BasicParticleType, maker: IParticleMaker<*>, callback: (IAnimatedSprite) -> Unit){
		ModParticles.Client.addFactory(type, maker, callback)
	}
}
