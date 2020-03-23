package chylex.hee.client.render.util
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.DestFactor
import com.mojang.blaze3d.platform.GlStateManager.FogMode
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor
import com.mojang.blaze3d.platform.GlStateManager.TexGen
import java.nio.FloatBuffer

typealias GLSM = GlStateManager

@Sided(Side.CLIENT)
object GL{
	
	// General
	
	fun depthMask(enable: Boolean) = GLSM.depthMask(enable)
	
	fun enableCull() = GLSM.enableCull()
	fun disableCull() = GLSM.disableCull()
	
	fun enableRescaleNormal() = GLSM.enableRescaleNormal()
	fun disableRescaleNormal() = GLSM.disableRescaleNormal()
	
	fun lineWidth(width: Float) = GLSM.lineWidth(width)
	
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
	
	// Lightmap
	
	fun setLightmapCoords(x: Float, y: Float){
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, x, y)
	}
	
	fun setLightmapCoords(x: Int, y: Int){
		setLightmapCoords(x.toFloat(), y.toFloat())
	}
	
	fun setLightmapCoords(brightness: Int){
		setLightmapCoords(brightness % 65536, brightness / 65536)
	}
	
	// Color
	
	fun enableColorMaterial() = GLSM.enableColorMaterial()
	fun disableColorMaterial() = GLSM.disableColorMaterial()
	
	fun color(red: Float, green: Float, blue: Float) = GLSM.color3f(red, green, blue)
	fun color(red: Float, green: Float, blue: Float, alpha: Float) = GLSM.color4f(red, green, blue, alpha)
	
	// Texture
	
	fun enableTexture() = GLSM.enableTexture()
	fun disableTexture() = GLSM.disableTexture()
	
	fun enableOutlineMode(color: Int) = GLSM.setupSolidRenderingTextureCombine(color)
	fun disableOutlineMode() = GLSM.tearDownSolidRenderingTextureCombine()
	
	fun enableTexGenCoord(tex: TexGen) = GLSM.enableTexGen(tex)
	fun disableTexGenCoord(tex: TexGen) = GLSM.disableTexGen(tex)
	
	fun texGenMode(tex: TexGen, mode: Int) = GLSM.texGenMode(tex, mode)
	fun texGenParam(tex: TexGen, param: Int, buffer: FloatBuffer) = GLSM.texGenParam(tex, param, buffer)
	
	// Matrix
	
	fun loadIdentity() = GLSM.loadIdentity()
	fun pushMatrix() = GLSM.pushMatrix()
	fun popMatrix() = GLSM.popMatrix()
	
	fun matrixMode(mode: Int) = GLSM.matrixMode(mode)
	
	fun translate(x: Float, y: Float, z: Float) = GLSM.translatef(x, y, z)
	fun translate(x: Double, y: Double, z: Double) = GLSM.translated(x, y, z)
	
	fun scale(x: Float, y: Float, z: Float) = GLSM.scalef(x, y, z)
	fun scale(x: Double, y: Double, z: Double) = GLSM.scaled(x, y, z)
	
	fun rotate(angle: Float, x: Float, y: Float, z: Float) = GLSM.rotatef(angle, x, y, z)
	fun rotate(angle: Double, x: Double, y: Double, z: Double) = GLSM.rotated(angle, x, y, z)
	
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
}
