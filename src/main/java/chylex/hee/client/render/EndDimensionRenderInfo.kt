package chylex.hee.client.render

import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec3
import net.minecraft.client.world.DimensionRenderInfo
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.client.ICloudRenderHandler
import net.minecraftforge.client.ISkyRenderHandler
import net.minecraftforge.client.IWeatherParticleRenderHandler
import net.minecraftforge.client.IWeatherRenderHandler

@Sided(Side.CLIENT)
object EndDimensionRenderInfo : DimensionRenderInfo.End() {
	fun register() {
		field_239208_a_[Resource.Custom("end")] = this
	}
	
	private val cloudRenderHandler = ICloudRenderHandler { _, _, _, _, _, _,_, _ -> }
	private val weatherRenderHandler = IWeatherRenderHandler { _, _, _, _, _, _, _, _ -> }
	private val weatherParticleRenderHandler = IWeatherParticleRenderHandler { _, _, _, _ -> }
	
	override fun func_239216_b_(): Boolean { // RENAME hasGround
		return false
	}
	
	override fun func_241684_d_(): Boolean { // RENAME forceBrightLightmap
		return false
	}
	
	override fun func_230494_a_(originalFogColor: Vector3d, brightness: Float): Vector3d { // RENAME getBrightnessDependentFogColor
		return TerritoryRenderer.environment?.fogColor ?: Vec3.ZERO
	}
	
	override fun getSkyRenderHandler(): ISkyRenderHandler {
		return TerritoryRenderer.environment?.renderer ?: EmptyRenderer
	}
	
	override fun getCloudRenderHandler(): ICloudRenderHandler {
		return cloudRenderHandler
	}
	
	override fun getWeatherRenderHandler(): IWeatherRenderHandler {
		return weatherRenderHandler
	}
	
	override fun getWeatherParticleRenderHandler(): IWeatherParticleRenderHandler {
		return weatherParticleRenderHandler
	}
}
