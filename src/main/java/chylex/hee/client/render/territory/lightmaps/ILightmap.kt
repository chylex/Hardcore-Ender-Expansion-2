package chylex.hee.client.render.territory.lightmaps

interface ILightmap{
	fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float)
	
	companion object{
		fun calcLightFactor(light: Float, mp: Float = 0.6F): Float{
			return light * ((((light * mp) + (1F - mp)) * mp) + (1F - mp))
		}
		
		fun calcLightSqFactor(light: Float, mp: Float = 0.6F): Float{
			return light * ((light * light * mp) + (1F - mp))
		}
	}
}
