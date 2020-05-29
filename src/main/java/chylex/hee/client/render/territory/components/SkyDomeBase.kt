package chylex.hee.client.render.territory.components
import chylex.hee.client.render.territory.AbstractEnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.offsetTowards
import chylex.hee.system.util.square
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.world.ClientWorld
import org.lwjgl.opengl.GL11.GL_FLAT
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_SMOOTH
import kotlin.math.pow
import kotlin.math.sqrt

abstract class SkyDomeBase : AbstractEnvironmentRenderer(){
	@Sided(Side.CLIENT)
	private object Skybox{
		data class Vertex(val x: Float, val y: Float, val z: Float, val c: Float, val u: Byte, val v: Byte)
		
		private const val SIZE = 8
		private const val COUNT = 15
		
		private fun yOffset(xp: Float, zp: Float): Float{
			return 32F - (1.15F * (square(xp) + square(zp)).pow(0.75F))
		}
		
		private fun yColor(xp: Float, zp: Float): Float{
			val distance = sqrt(square(xp) + square(zp)) / (COUNT - 2F)
			val stretched = 1F - ((distance - 0.4F) / 0.6F)
			
			return square(stretched.coerceIn(0F, 1F))
		}
		
		val VERTICES = lazy {
			val list = mutableListOf<Vertex>()
			
			for(xi in -COUNT..COUNT){
				for(zi in -COUNT..COUNT){
					if (square(xi) + square(zi) < square(COUNT)){
						val x1 = ((xi * SIZE) - SIZE / 2).toFloat()
						val x2 = ((xi * SIZE) + SIZE / 2).toFloat()
						
						val z1 = ((zi * SIZE) - SIZE / 2).toFloat()
						val z2 = ((zi * SIZE) + SIZE / 2).toFloat()
						
						val y11 = yOffset(xi - 0.5F, zi - 0.5F)
						val y12 = yOffset(xi - 0.5F, zi + 0.5F)
						val y21 = yOffset(xi + 0.5F, zi - 0.5F)
						val y22 = yOffset(xi + 0.5F, zi + 0.5F)
						
						val c11 = yColor(xi - 0.5F, zi - 0.5F)
						val c12 = yColor(xi - 0.5F, zi + 0.5F)
						val c21 = yColor(xi + 0.5F, zi - 0.5F)
						val c22 = yColor(xi + 0.5F, zi + 0.5F)
						
						list.add(Vertex(x = x1, y = y11, z = z1, c = c11, u = 0, v = 0))
						list.add(Vertex(x = x2, y = y21, z = z1, c = c21, u = 0, v = 3))
						list.add(Vertex(x = x2, y = y22, z = z2, c = c22, u = 3, v = 3))
						list.add(Vertex(x = x1, y = y12, z = z2, c = c12, u = 3, v = 0))
					}
				}
			}
			
			list.toList()
		}
	}
	
	protected open val texture = DEFAULT_TEXTURE
	protected open val color1 = DEFAULT_COLOR
	protected open val color2 = DEFAULT_COLOR
	protected open val alpha1 = DEFAULT_ALPHA
	protected open val alpha2 = DEFAULT_ALPHA
	
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, partialTicks: Float){
		val color1 = color1
		val color2 = color2
		val alpha1 = alpha1 * currentSkyAlpha
		val alpha2 = alpha2 * currentSkyAlpha
		
		val r1 = color1.x.toFloat()
		val g1 = color1.y.toFloat()
		val b1 = color1.z.toFloat()
		
		val r2 = color2.x.toFloat()
		val g2 = color2.y.toFloat()
		val b2 = color2.z.toFloat()
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.enableFog()
		GL.shadeModel(GL_SMOOTH)
		
		GL.enableTexture()
		MC.textureManager.bindTexture(texture)
		
		TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR){
			for((x, y, z, c, u, v) in Skybox.VERTICES.value){
				val r = offsetTowards(r2, r1, c)
				val g = offsetTowards(g2, g1, c)
				val b = offsetTowards(b2, b1, c)
				val a = offsetTowards(alpha2, alpha1, c)
				
				pos(x.toDouble(), y.toDouble(), z.toDouble())
				tex(u.toDouble(), v.toDouble())
				color(r, g, b, a)
				endVertex()
			}
		}
		
		GL.shadeModel(GL_FLAT)
		GL.disableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
