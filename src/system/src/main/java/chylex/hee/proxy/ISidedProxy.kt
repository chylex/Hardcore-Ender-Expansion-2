package chylex.hee.proxy
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.particles.BasicParticleType

interface ISidedProxy{
	fun getClientSidePlayer(): EntityPlayer? = null
	
	fun registerParticle(type: BasicParticleType, maker: IParticleMaker<*>, callback: (IAnimatedSprite) -> Unit){}
}
