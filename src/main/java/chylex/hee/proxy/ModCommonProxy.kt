package chylex.hee.proxy
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.vanilla.EntityPlayer
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.particles.BasicParticleType

open class ModCommonProxy{
	open fun getClientSidePlayer(): EntityPlayer? = null
	
	open fun registerParticle(type: BasicParticleType, maker: IParticleMaker<*>, callback: (IAnimatedSprite) -> Unit){}
}
