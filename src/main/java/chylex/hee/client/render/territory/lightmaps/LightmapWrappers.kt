package chylex.hee.client.render.territory.lightmaps
import kotlin.math.max

fun ILightmap.mergeSkyLightIntoBlockLight() = object : ILightmap{
	override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		this@mergeSkyLightIntoBlockLight.update(colors, sunBrightness, skyLight, max(skyLight, blockLight), partialTicks)
	}
}

fun ILightmap.transformSunBrightness(transformer: (Float) -> Float) = object : ILightmap{
	override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		this@transformSunBrightness.update(colors, transformer(sunBrightness), skyLight, blockLight, partialTicks)
	}
}

fun ILightmap.transformSkyLight(transformer: (Float) -> Float) = object : ILightmap{
	override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		this@transformSkyLight.update(colors, sunBrightness, transformer(skyLight), blockLight, partialTicks)
	}
}

fun ILightmap.transformBlockLight(transformer: (Float) -> Float) = object : ILightmap{
	override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		this@transformBlockLight.update(colors, sunBrightness, skyLight, transformer(blockLight), partialTicks)
	}
}

fun ILightmap.transformColors(red: (Float) -> Float, green: (Float) -> Float, blue: (Float) -> Float) = object : ILightmap{
	override fun update(colors: FloatArray, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float){
		this@transformColors.update(colors, sunBrightness, skyLight, blockLight, partialTicks)
		
		colors[0] = red(colors[0])
		colors[1] = green(colors[1])
		colors[2] = blue(colors[2])
	}
}
