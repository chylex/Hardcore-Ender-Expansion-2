package chylex.hee.client.render.territory.components
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.system.util.square
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_FLAT
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_SMOOTH
import kotlin.math.pow
import kotlin.math.sqrt

abstract class SkyDomeBase : IRenderHandler(){
	@SideOnly(Side.CLIENT)
	private object Skybox{
		data class Vertex(val x: Float, val y: Float, val z: Float, val u: Float, val v: Float, val c: Float)
		
		val VERTICES = lazy {
			val list = mutableListOf<Vertex>()
			
			val size = 8
			val count = 15
			
			fun yOffset(xp: Float, zp: Float): Float{
				return 32F - (1.15F * (square(xp) + square(zp)).pow(0.75F))
			}
			
			fun yColor(xp: Float, zp: Float): Float{
				val distance = sqrt(square(xp) + square(zp)) / (count - 2F)
				val stretched = 1F - ((distance - 0.4F) / 0.6F)
				
				return stretched.coerceIn(0F, 1F)
			}
			
			for(xi in -count..count){
				for(zi in -count..count){
					if (square(xi) + square(zi) < square(count)){
						val x1 = ((xi * size) - size / 2).toFloat()
						val x2 = ((xi * size) + size / 2).toFloat()
						
						val z1 = ((zi * size) - size / 2).toFloat()
						val z2 = ((zi * size) + size / 2).toFloat()
						
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
	
	@SideOnly(Side.CLIENT)
	override fun render(partialTicks: Float, world: WorldClient, mc: Minecraft){
		val col = color
		val alp = alpha
		
		val red = col.x.toFloat()
		val green = col.y.toFloat()
		val blue = col.z.toFloat()
		
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		
		GL.enableBlend()
		GL.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.enableFog()
		GL.shadeModel(GL_SMOOTH)
		
		GL.enableTexture2D()
		MC.textureManager.bindTexture(texture)
		
		buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
		
		for((x, y, z, u, v, c) in Skybox.VERTICES.value){
			buffer
				.pos(x.toDouble(), y.toDouble(), z.toDouble())
				.tex(u.toDouble(), v.toDouble())
				.color(red * c, green * c, blue * c, alp)
				.endVertex()
		}
		
		tessellator.draw()
		
		GL.shadeModel(GL_FLAT)
		GL.disableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
