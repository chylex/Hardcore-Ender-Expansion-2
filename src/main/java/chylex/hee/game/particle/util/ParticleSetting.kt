package chylex.hee.game.particle.util
import chylex.hee.client.util.MC
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

enum class ParticleSetting{
	ALL,
	DECREASED,
	MINIMAL;
	
	companion object{
		val current
			@SideOnly(Side.CLIENT)
			get() = when(MC.settings.particleSetting){
				1 -> DECREASED
				2 -> MINIMAL
				else -> ALL
			}
	}
}
