package chylex.hee.client.render.territory.components
import chylex.hee.client.render.territory.EnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_QUADS

abstract class SkyCubeBase : IRenderHandler{
	protected companion object{
		const val DEFAULT_ALPHA = 1F
		const val DEFAULT_DISTANCE = 125.0
	}
	
	protected abstract val texture: ResourceLocation
	protected abstract val color: Vec3d
	protected open val alpha = DEFAULT_ALPHA
	protected open val distance = DEFAULT_DISTANCE
	
	@Sided(Side.CLIENT)
	override fun render(ticks: Int, partialTicks: Float, world: ClientWorld, mc: Minecraft){
		val dist = distance.coerceAtMost(18.5 * mc.gameSettings.renderDistanceChunks)
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		RenderHelper.disableStandardItemLighting()
		
		GL.color(color, alpha * EnvironmentRenderer.currentSkyAlpha)
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
			
			TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX){
				pos(-dist, -dist, -dist).tex( 0.0,  0.0).endVertex()
				pos(-dist, -dist,  dist).tex( 0.0, 16.0).endVertex()
				pos( dist, -dist,  dist).tex(16.0, 16.0).endVertex()
				pos( dist, -dist, -dist).tex(16.0,  0.0).endVertex()
			}
			
			GL.popMatrix()
		}
		
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
