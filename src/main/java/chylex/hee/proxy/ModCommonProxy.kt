package chylex.hee.proxy
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.particles.BasicParticleType

open class ModCommonProxy : ISidedProxy{
	override fun getClientSidePlayer(): EntityPlayer? = null
	
	override fun registerParticle(type: BasicParticleType, maker: IParticleMaker<*>, callback: (IAnimatedSprite) -> Unit){}
}
