@file:Suppress("DEPRECATION")

package chylex.hee.client.render.gl
import chylex.hee.client.MC
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.DestFactor
import com.mojang.blaze3d.platform.GlStateManager.FogMode
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor
import com.mojang.blaze3d.platform.GlStateManager.TexGen
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.vector.Vector3d
import java.nio.FloatBuffer

private typealias GLSM = RenderSystem

@Sided(Side.CLIENT)
object GL{
	
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
	fun disableTexture() = GLSM.disableTexture()
	
	fun bindTexture(texture: ResourceLocation) = MC.textureManager.bindTexture(texture)
	
	fun enableTexGenCoord(tex: TexGen) = GlStateManager.enableTexGen(tex)
	fun disableTexGenCoord(tex: TexGen) = GlStateManager.disableTexGen(tex)
	
	fun texGenMode(tex: TexGen, mode: Int) = GlStateManager.texGenMode(tex, mode)
	fun texGenParam(tex: TexGen, param: Int, buffer: FloatBuffer) = GlStateManager.texGenParam(tex, param, buffer)
	
	// Matrix
	
	fun loadIdentity() = GLSM.loadIdentity()
	fun pushMatrix() = GLSM.pushMatrix()
	fun popMatrix() = GLSM.popMatrix()
	
	fun matrixMode(mode: Int) = GLSM.matrixMode(mode)
	
	fun translate(x: Float, y: Float, z: Float) = GLSM.translatef(x, y, z)
	fun translate(x: Double, y: Double, z: Double) = GLSM.translated(x, y, z)
	
	fun scale(x: Float, y: Float, z: Float) = GLSM.scalef(x, y, z)
	
	fun rotate(angle: Float, x: Float, y: Float, z: Float) = GLSM.rotatef(angle, x, y, z)
}

// Constants

val TEX_S = TexGen.S
val TEX_T = TexGen.T
val TEX_R = TexGen.R
val TEX_Q = TexGen.Q

val SF_CONSTANT_ALPHA = SourceFactor.CONSTANT_ALPHA
val SF_CONSTANT_COLOR = SourceFactor.CONSTANT_COLOR
val SF_DST_ALPHA = SourceFactor.DST_ALPHA
val SF_DST_COLOR = SourceFactor.DST_COLOR
val SF_ONE = SourceFactor.ONE
val SF_ONE_MINUS_CONSTANT_ALPHA = SourceFactor.ONE_MINUS_CONSTANT_ALPHA
val SF_ONE_MINUS_CONSTANT_COLOR = SourceFactor.ONE_MINUS_CONSTANT_COLOR
val SF_ONE_MINUS_DST_ALPHA = SourceFactor.ONE_MINUS_DST_ALPHA
val SF_ONE_MINUS_DST_COLOR = SourceFactor.ONE_MINUS_DST_COLOR
val SF_ONE_MINUS_SRC_ALPHA = SourceFactor.ONE_MINUS_SRC_ALPHA
val SF_ONE_MINUS_SRC_COLOR = SourceFactor.ONE_MINUS_SRC_COLOR
val SF_SRC_ALPHA = SourceFactor.SRC_ALPHA
val SF_SRC_ALPHA_SATURATE = SourceFactor.SRC_ALPHA_SATURATE
val SF_SRC_COLOR = SourceFactor.SRC_COLOR
val SF_ZERO = SourceFactor.ZERO

val DF_CONSTANT_ALPHA = DestFactor.CONSTANT_ALPHA
val DF_CONSTANT_COLOR = DestFactor.CONSTANT_COLOR
val DF_DST_ALPHA = DestFactor.DST_ALPHA
val DF_DST_COLOR = DestFactor.DST_COLOR
val DF_ONE = DestFactor.ONE
val DF_ONE_MINUS_CONSTANT_ALPHA = DestFactor.ONE_MINUS_CONSTANT_ALPHA
val DF_ONE_MINUS_CONSTANT_COLOR = DestFactor.ONE_MINUS_CONSTANT_COLOR
val DF_ONE_MINUS_DST_ALPHA = DestFactor.ONE_MINUS_DST_ALPHA
val DF_ONE_MINUS_DST_COLOR = DestFactor.ONE_MINUS_DST_COLOR
val DF_ONE_MINUS_SRC_ALPHA = DestFactor.ONE_MINUS_SRC_ALPHA
val DF_ONE_MINUS_SRC_COLOR = DestFactor.ONE_MINUS_SRC_COLOR
val DF_SRC_ALPHA = DestFactor.SRC_ALPHA
val DF_SRC_COLOR = DestFactor.SRC_COLOR
val DF_ZERO = DestFactor.ZERO

val FOG_LINEAR = FogMode.LINEAR
val FOG_EXP = FogMode.EXP
val FOG_EXP2 = FogMode.EXP2
