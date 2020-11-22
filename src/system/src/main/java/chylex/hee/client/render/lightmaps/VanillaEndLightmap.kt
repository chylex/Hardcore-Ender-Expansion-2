package chylex.hee.client.render.lightmaps
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightSqFactor
import net.minecraft.util.math.vector.Vector3f

object VanillaEndLightmap : ILightmap{
	override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		val blockFactor = calcLightFactor(blockLight)
		val blockSqFactor = calcLightSqFactor(blockLight)
		
		colors.x = (blockLight * 0.75F) + 0.22F
		colors.y = (blockFactor * 0.75F) + 0.28F
		colors.z = (blockSqFactor * 0.75F) + 0.25F
	}
}
