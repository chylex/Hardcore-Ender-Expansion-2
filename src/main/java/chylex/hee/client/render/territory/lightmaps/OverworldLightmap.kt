package chylex.hee.client.render.territory.lightmaps
import chylex.hee.client.render.territory.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.territory.lightmaps.ILightmap.Companion.calcLightSqFactor

object OverworldLightmap : ILightmap{
	override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		val sunFactor = skyLight * ((sunBrightness * 0.65F) + 0.35F)
		val blockFactor = calcLightFactor(blockLight)
		val blockSqFactor = calcLightSqFactor(blockLight)
		
		val red = sunFactor + blockLight
		val green = sunFactor + blockFactor
		val blue = skyLight + blockSqFactor
		
		colors[0] = (red * 0.96F) + 0.03F
		colors[1] = (green * 0.96F) + 0.03F
		colors[2] = (blue * 0.96F) + 0.03F
	}
}
