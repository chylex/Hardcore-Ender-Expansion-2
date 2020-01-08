package chylex.hee.client.render.territory.components
import chylex.hee.client.render.territory.EnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.square
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import org.lwjgl.opengl.GL11.GL_FLAT
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_SMOOTH
import kotlin.math.pow
import kotlin.math.sqrt

abstract class SkyDomeBase : IRenderHandler{
	@Sided(Side.CLIENT)
	private object Skybox{
		data class Vertex(val x: Float, val y: Float, val z: Float, val u: Float, val v: Float, val c: Float)
		
		private const val SIZE = 8
		private const val COUNT = 15
		
		private fun yOffset(xp: Float, zp: Float): Float{
			return 32F - (1.15F * (square(xp) + square(zp)).pow(0.75F))
		}
		
		private fun yColor(xp: Float, zp: Float): Float{
			val distance = sqrt(square(xp) + square(zp)) / (COUNT - 2F)
			val stretched = 1F - ((distance - 0.4F) / 0.6F)
			
			return stretched.coerceIn(0F, 1F)
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
						
						list.add(Vertex(x = x1, y = y11, z = z1, u = 0F, v = 0F, c = c11))
						list.add(Vertex(x = x2, y = y21, z = z1, u = 0F, v = 3F, c = c21))
						list.add(Vertex(x = x2, y = y22, z = z2, u = 3F, v = 3F, c = c22))
						list.add(Vertex(x = x1, y = y12, z = z2, u = 3F, v = 0F, c = c12))
					}
				}
			}
			
			list.toList()
		}
	}
	
	protected companion object{
		const val DEFAULT_ALPHA = 1F
	}
	
	protected abstract val texture: ResourceLocation
	protected abstract val color: Vec3d
	protected open val alpha = DEFAULT_ALPHA
	
	@Sided(Side.CLIENT)
	override fun render(ticks: Int, partialTicks: Float, world: ClientWorld, mc: Minecraft){
		val col = color
		val alp = alpha * EnvironmentRenderer.currentSkyAlpha
		
		val red = col.x.toFloat()
		val green = col.y.toFloat()
		val blue = col.z.toFloat()
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.enableFog()
		GL.shadeModel(GL_SMOOTH)
		
		GL.enableTexture()
		MC.textureManager.bindTexture(texture)
		
		TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR){
			for((x, y, z, u, v, c) in Skybox.VERTICES.value){
				pos(x.toDouble(), y.toDouble(), z.toDouble())
				tex(u.toDouble(), v.toDouble())
				color(red * c, green * c, blue * c, alp)
				endVertex()
			}
		}
		
		GL.shadeModel(GL_FLAT)
		GL.disableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
