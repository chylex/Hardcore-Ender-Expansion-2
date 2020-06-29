package chylex.hee.client.render.territory.lightmaps
import net.minecraft.client.renderer.Vector3f

interface ILightmap{
	fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float)
	
	companion object{
		fun calcLightFactor(light: Float, mp: Float = 0.6F): Float{
			return light * ((((light * mp) + (1F - mp)) * mp) + (1F - mp))
		}
		
		fun calcLightSqFactor(light: Float, mp: Float = 0.6F): Float{
			return light * ((light * light * mp) + (1F - mp))
		}
	}
}
