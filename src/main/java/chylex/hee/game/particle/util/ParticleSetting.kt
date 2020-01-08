package chylex.hee.game.particle.util
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.settings.ParticleStatus

enum class ParticleSetting{
	ALL,
	DECREASED,
	MINIMAL;
	
	companion object{
		val current
			@Sided(Side.CLIENT)
			get() = when(MC.settings.particles){
				ParticleStatus.MINIMAL -> MINIMAL
				ParticleStatus.DECREASED -> DECREASED
				else -> ALL
			}
	}
}
