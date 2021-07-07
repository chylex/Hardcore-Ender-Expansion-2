@file:Suppress("DEPRECATION")

package chylex.hee.client.render.util

import chylex.hee.client.util.MC
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.platform.GlStateManager.DestFactor
import com.mojang.blaze3d.platform.GlStateManager.FogMode
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.vector.Vector3d

private typealias GLSM = RenderSystem

@Sided(Side.CLIENT)
object GL {
	
	// General
	
	fun depthMask(enable: Boolean) = GLSM.depthMask(enable)
	
	// Blend
	
	fun enableBlend() = GLSM.enableBlend()
	fun disableBlend() = GLSM.disableBlend()
	
	fun blendFunc(s: SourceFactor, d: DestFactor) = GLSM.blendFunc(s, d)
	fun blendFunc(srgb: SourceFactor, drgb: DestFactor, sa: SourceFactor, da: DestFactor) = GLSM.blendFuncSeparate(srgb, drgb, sa, da)
	
	// Alpha
	
	fun enableAlpha() = GLSM.enableAlphaTest()
	fun disableAlpha() = GLSM.disableAlphaTest()
	
	fun alphaFunc(func: Int, ref: Float) = GLSM.alphaFunc(func, ref)
	
	// Fog
	
	fun enableFog() = GLSM.enableFog()
	fun disableFog() = GLSM.disableFog()
	
	fun setFogMode(mode: FogMode) = GLSM.fogMode(mode)
	fun setFogDensity(density: Float) = GLSM.fogDensity(density)
	
	// Lighting
	
	fun enableLighting() = GLSM.enableLighting()
	fun disableLighting() = GLSM.disableLighting()
	
	fun shadeModel(model: Int) = GLSM.shadeModel(model)
	
	// Color
	
	fun color(red: Float, green: Float, blue: Float, alpha: Float) = GLSM.color4f(red, green, blue, alpha)
	fun color(color: Vector3d, alpha: Float) = GLSM.color4f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat(), alpha)
	
	// Texture
	
	fun enableTexture() = GLSM.enableTexture()
	fun bindTexture(texture: ResourceLocation) = MC.textureManager.bindTexture(texture)
}

// Constants

val SF_CONSTANT_ALPHA get() = SourceFactor.CONSTANT_ALPHA
val SF_CONSTANT_COLOR get() = SourceFactor.CONSTANT_COLOR
val SF_DST_ALPHA get() = SourceFactor.DST_ALPHA
val SF_DST_COLOR get() = SourceFactor.DST_COLOR
val SF_ONE get() = SourceFactor.ONE
val SF_ONE_MINUS_CONSTANT_ALPHA get() = SourceFactor.ONE_MINUS_CONSTANT_ALPHA
val SF_ONE_MINUS_CONSTANT_COLOR get() = SourceFactor.ONE_MINUS_CONSTANT_COLOR
val SF_ONE_MINUS_DST_ALPHA get() = SourceFactor.ONE_MINUS_DST_ALPHA
val SF_ONE_MINUS_DST_COLOR get() = SourceFactor.ONE_MINUS_DST_COLOR
val SF_ONE_MINUS_SRC_ALPHA get() = SourceFactor.ONE_MINUS_SRC_ALPHA
val SF_ONE_MINUS_SRC_COLOR get() = SourceFactor.ONE_MINUS_SRC_COLOR
val SF_SRC_ALPHA get() = SourceFactor.SRC_ALPHA
val SF_SRC_ALPHA_SATURATE get() = SourceFactor.SRC_ALPHA_SATURATE
val SF_SRC_COLOR get() = SourceFactor.SRC_COLOR
val SF_ZERO get() = SourceFactor.ZERO

val DF_CONSTANT_ALPHA get() = DestFactor.CONSTANT_ALPHA
val DF_CONSTANT_COLOR get() = DestFactor.CONSTANT_COLOR
val DF_DST_ALPHA get() = DestFactor.DST_ALPHA
val DF_DST_COLOR get() = DestFactor.DST_COLOR
val DF_ONE get() = DestFactor.ONE
val DF_ONE_MINUS_CONSTANT_ALPHA get() = DestFactor.ONE_MINUS_CONSTANT_ALPHA
val DF_ONE_MINUS_CONSTANT_COLOR get() = DestFactor.ONE_MINUS_CONSTANT_COLOR
val DF_ONE_MINUS_DST_ALPHA get() = DestFactor.ONE_MINUS_DST_ALPHA
val DF_ONE_MINUS_DST_COLOR get() = DestFactor.ONE_MINUS_DST_COLOR
val DF_ONE_MINUS_SRC_ALPHA get() = DestFactor.ONE_MINUS_SRC_ALPHA
val DF_ONE_MINUS_SRC_COLOR get() = DestFactor.ONE_MINUS_SRC_COLOR
val DF_SRC_ALPHA get() = DestFactor.SRC_ALPHA
val DF_SRC_COLOR get() = DestFactor.SRC_COLOR
val DF_ZERO get() = DestFactor.ZERO
