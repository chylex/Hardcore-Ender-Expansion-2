package chylex.hee.client.render.lightmaps
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightSqFactor
import net.minecraft.util.math.vector.Vector3f

object OverworldLightmap : ILightmap{
	override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		val sunFactor = skyLight * ((sunBrightness * 0.65F) + 0.35F)
		val blockFactor = calcLightFactor(blockLight)
		val blockSqFactor = calcLightSqFactor(blockLight)
		
		val red = sunFactor + blockLight
		val green = sunFactor + blockFactor
		val blue = skyLight + blockSqFactor
		
		colors.x = (red * 0.96F) + 0.03F
		colors.y = (green * 0.96F) + 0.03F
		colors.z = (blue * 0.96F) + 0.03F
	}
}
