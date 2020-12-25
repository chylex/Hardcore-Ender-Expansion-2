package chylex.hee.client.render.lightmaps

import net.minecraft.client.renderer.Vector3f
import kotlin.math.max

fun ILightmap.mergeSkyLightIntoBlockLight() = object : ILightmap {
	override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float) {
		this@mergeSkyLightIntoBlockLight.update(colors, sunBrightness, skyLight, max(skyLight, blockLight), partialTicks)
	}
}

fun ILightmap.transformSunBrightness(transformer: (Float) -> Float) = object : ILightmap {
	override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float) {
		this@transformSunBrightness.update(colors, transformer(sunBrightness), skyLight, blockLight, partialTicks)
	}
}

fun ILightmap.transformSkyLight(transformer: (Float) -> Float) = object : ILightmap {
	override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float) {
		this@transformSkyLight.update(colors, sunBrightness, transformer(skyLight), blockLight, partialTicks)
	}
}

fun ILightmap.transformBlockLight(transformer: (Float) -> Float) = object : ILightmap {
	override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float) {
		this@transformBlockLight.update(colors, sunBrightness, skyLight, transformer(blockLight), partialTicks)
	}
}

fun ILightmap.transformColors(red: (Float) -> Float, green: (Float) -> Float, blue: (Float) -> Float) = object : ILightmap {
	override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float) {
		this@transformColors.update(colors, sunBrightness, skyLight, blockLight, partialTicks)
		
		colors.x = red(colors.x)
		colors.y = green(colors.y)
		colors.z = blue(colors.z)
	}
}
