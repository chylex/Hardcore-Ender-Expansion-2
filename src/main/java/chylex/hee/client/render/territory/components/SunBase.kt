package chylex.hee.client.render.territory.components
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.GlStateManager.DestFactor
import net.minecraft.client.renderer.GlStateManager.SourceFactor
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
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
	
	@SideOnly(Side.CLIENT)
	override fun render(partialTicks: Float, world: WorldClient, mc: Minecraft){
		val width = size
		val dist = distance
		val col = color
		
		val red = col.x.toFloat()
		val green = col.y.toFloat()
		val blue = col.z.toFloat()
		
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		
		GL.enableBlend()
		GL.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		GL.pushMatrix()
		
		setRotation(world, partialTicks)
		
		GL.color(red, green, blue, alpha)
		MC.textureManager.bindTexture(texture)
		
		buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
		buffer.pos(-width, dist, -width).tex(0.0, 0.0).endVertex()
		buffer.pos( width, dist, -width).tex(1.0, 0.0).endVertex()
		buffer.pos( width, dist,  width).tex(1.0, 1.0).endVertex()
		buffer.pos(-width, dist,  width).tex(0.0, 1.0).endVertex()
		tessellator.draw()
		
		GL.popMatrix()
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
