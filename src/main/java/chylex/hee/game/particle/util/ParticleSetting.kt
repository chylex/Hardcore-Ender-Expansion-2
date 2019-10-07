package chylex.hee.game.particle.util
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided

enum class ParticleSetting{
	ALL,
	DECREASED,
	MINIMAL;
	
	companion object{
		val current
			@Sided(Side.CLIENT)
			get() = when(MC.settings.particleSetting){
				1 -> DECREASED
				2 -> MINIMAL
				else -> ALL
			}
	}
}
