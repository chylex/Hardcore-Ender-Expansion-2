package chylex.hee.game.particle.util
import net.minecraft.client.settings.GameSettings
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

enum class ParticleSetting{
	ALL,
	DECREASED,
	MINIMAL;
	
	companion object{
		@SideOnly(Side.CLIENT)
		fun get(settings: GameSettings): ParticleSetting = when(settings.particleSetting){
			1 -> DECREASED
			2 -> MINIMAL
			else -> ALL
		}
	}
}
