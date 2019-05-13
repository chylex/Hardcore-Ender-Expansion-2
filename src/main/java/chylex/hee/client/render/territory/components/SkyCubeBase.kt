package chylex.hee.client.render.territory.components
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
import net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.GL_GREATER

abstract class SkyCubeBase : IRenderHandler(){
	protected companion object{
		const val DEFAULT_ALPHA = 1F
		const val DEFAULT_DISTANCE = 125.0
	}
	
	protected abstract val texture: ResourceLocation
	protected abstract val color: Vec3d
	protected open val alpha = DEFAULT_ALPHA
	protected open val distance = DEFAULT_DISTANCE
	
	@SideOnly(Side.CLIENT)
	override fun render(partialTicks: Float, world: WorldClient, mc: Minecraft){
		val dist = distance
		val col = color
		
		val red = col.x.toFloat()
		val green = col.y.toFloat()
		val blue = col.z.toFloat()
		
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		
		GL.enableBlend()
		GL.tryBlendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		RenderHelper.disableStandardItemLighting()
		
		GL.color(red, green, blue, alpha)
		MC.textureManager.bindTexture(texture)
		
		for(side in 0..5){
			GL.pushMatrix()
			
			when(side){
				1 -> GL.rotate( 90F, 1F, 0F, 0F)
				2 -> GL.rotate(-90F, 1F, 0F, 0F)
				3 -> GL.rotate(180F, 1F, 0F, 0F)
				4 -> GL.rotate( 90F, 0F, 0F, 1F)
				5 -> GL.rotate(-90F, 0F, 0F, 1F)
			}
			
			buffer.begin(7, DefaultVertexFormats.POSITION_TEX)
			buffer.pos(-dist, -dist, -dist).tex( 0.0,  0.0).endVertex()
			buffer.pos(-dist, -dist,  dist).tex( 0.0, 16.0).endVertex()
			buffer.pos( dist, -dist,  dist).tex(16.0, 16.0).endVertex()
			buffer.pos( dist, -dist, -dist).tex(16.0,  0.0).endVertex()
			tessellator.draw()
			
			GL.popMatrix()
		}
		
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
