package chylex.hee.client.render.util
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.DestFactor
import net.minecraft.client.renderer.GlStateManager.FogMode
import net.minecraft.client.renderer.GlStateManager.SourceFactor
import net.minecraft.client.renderer.GlStateManager.TexGen
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.nio.FloatBuffer

typealias GLSM = GlStateManager

@SideOnly(Side.CLIENT)
object GL{
	
	// General
	
	fun depthMask(enable: Boolean) = GLSM.depthMask(enable)
	
	fun enableCull() = GLSM.enableCull()
	fun disableCull() = GLSM.disableCull()
	
	fun enableRescaleNormal() = GLSM.enableRescaleNormal()
	fun disableRescaleNormal() = GLSM.disableRescaleNormal()
	
	// Blend
	
	fun enableBlend() = GLSM.enableBlend()
	fun disableBlend() = GLSM.disableBlend()
	
	fun blendFunc(s: SourceFactor, d: DestFactor) = GLSM.blendFunc(s, d)
	fun blendFunc(srgb: SourceFactor, drgb: DestFactor, sa: SourceFactor, da: DestFactor) = GLSM.tryBlendFuncSeparate(srgb, drgb, sa, da)
	
	// Alpha
	
	fun enableAlpha() = GLSM.enableAlpha()
	fun disableAlpha() = GLSM.disableAlpha()
	
	fun alphaFunc(func: Int, ref: Float) = GLSM.alphaFunc(func, ref)
	
	// Fog
	
	fun enableFog() = GLSM.enableFog()
	fun disableFog() = GLSM.disableFog()
	
	fun setFogMode(mode: FogMode) = GLSM.setFog(mode)
	fun setFogDensity(density: Float) = GLSM.setFogDensity(density)
	
	// Lighting
	
	fun enableLighting() = GLSM.enableLighting()
	fun disableLighting() = GLSM.disableLighting()
	
	fun shadeModel(model: Int) = GLSM.shadeModel(model)
	
	// Color
	
	fun enableColorMaterial() = GLSM.enableColorMaterial()
	fun disableColorMaterial() = GLSM.disableColorMaterial()
	
	fun color(red: Float, green: Float, blue: Float) = GLSM.color(red, green, blue)
	fun color(red: Float, green: Float, blue: Float, alpha: Float) = GLSM.color(red, green, blue, alpha)
	
	// Texture
	
	fun enableTexture2D() = GLSM.enableTexture2D()
	fun disableTexture2D() = GLSM.disableTexture2D()
	
	fun enableOutlineMode(color: Int) = GLSM.enableOutlineMode(color)
	fun disableOutlineMode() = GLSM.disableOutlineMode()
	
	fun enableTexGenCoord(tex: TexGen) = GLSM.enableTexGenCoord(tex)
	fun disableTexGenCoord(tex: TexGen) = GLSM.disableTexGenCoord(tex)
	
	fun texGenMode(tex: TexGen, mode: Int) = GLSM.texGen(tex, mode)
	fun texGenParam(tex: TexGen, param: Int, buffer: FloatBuffer) = GLSM.texGen(tex, param, buffer)
	
	// Matrix
	
	fun loadIdentity() = GLSM.loadIdentity()
	fun pushMatrix() = GLSM.pushMatrix()
	fun popMatrix() = GLSM.popMatrix()
	
	fun matrixMode(mode: Int) = GLSM.matrixMode(mode)
	
	fun translate(x: Float, y: Float, z: Float) = GLSM.translate(x, y, z)
	fun translate(x: Double, y: Double, z: Double) = GLSM.translate(x, y, z)
	
	fun scale(x: Float, y: Float, z: Float) = GLSM.scale(x, y, z)
	fun scale(x: Double, y: Double, z: Double) = GLSM.scale(x, y, z)
	
	fun rotate(angle: Float, x: Float, y: Float, z: Float) = GLSM.rotate(angle, x, y, z)
	// fun rotate(angle: Double, x: Double, y: Double, z: Double) = GLSM.rotate(angle, x, y, z)
	
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
