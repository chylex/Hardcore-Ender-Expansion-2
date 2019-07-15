package chylex.hee.client.render.territory.lightmaps
import chylex.hee.client.render.territory.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.territory.lightmaps.ILightmap.Companion.calcLightSqFactor

object VanillaEndLightmap : ILightmap{
	override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		val blockFactor = calcLightFactor(blockLight)
		val blockSqFactor = calcLightSqFactor(blockLight)
		
		colors[0] = (blockLight * 0.75F) + 0.22F
		colors[1] = (blockFactor * 0.75F) + 0.28F
		colors[2] = (blockSqFactor * 0.75F) + 0.25F
	}
}
