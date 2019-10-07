package chylex.hee.client.render.territory.components
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_QUADS

abstract class SunBase : IRenderHandler(){
	protected companion object{
		val DEFAULT_COLOR = Vec3d(1.0, 1.0, 1.0)
		const val DEFAULT_ALPHA = 1F
		const val DEFAULT_DISTANCE = 100.0
	}
	
	protected abstract val texture: ResourceLocation
	protected open val color = DEFAULT_COLOR
	protected open val alpha = DEFAULT_ALPHA
	protected abstract val size: Double
	protected open val distance = DEFAULT_DISTANCE
	
	protected open fun setRotation(world: WorldClient, partialTicks: Float){
		GL.rotate(-90F, 0F, 1F, 0F)
		GL.rotate(world.getCelestialAngle(partialTicks) * 360F, 1F, 0F, 0F)
	}
	
	@Sided(Side.CLIENT)
	override fun render(partialTicks: Float, world: WorldClient, mc: Minecraft){
		val width = size
		val dist = distance
		val col = color
		
		val red = col.x.toFloat()
		val green = col.y.toFloat()
		val blue = col.z.toFloat()
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		GL.pushMatrix()
		
		setRotation(world, partialTicks)
		
		GL.color(red, green, blue, alpha)
		MC.textureManager.bindTexture(texture)
		
		TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX){
			pos(-width, dist, -width).tex(0.0, 0.0).endVertex()
			pos( width, dist, -width).tex(1.0, 0.0).endVertex()
			pos( width, dist,  width).tex(1.0, 1.0).endVertex()
			pos(-width, dist,  width).tex(0.0, 1.0).endVertex()
		}
		
		GL.popMatrix()
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
