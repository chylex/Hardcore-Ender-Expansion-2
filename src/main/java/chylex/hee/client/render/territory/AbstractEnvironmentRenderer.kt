package chylex.hee.client.render.territory
import chylex.hee.client.render.TerritoryRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.remapRange
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.SkyRenderHandler
import org.lwjgl.opengl.GL11.GL_QUADS
import kotlin.math.pow

abstract class AbstractEnvironmentRenderer : SkyRenderHandler{
	companion object{
		val currentSkyAlpha
			@Sided(Side.CLIENT)
			get() = remapRange(TerritoryRenderer.VOID_FACTOR_VALUE, (-1F)..(0.5F), (1F)..(0F)).coerceIn(0F, 1F)
		
		val currentFogDensityMp
			@Sided(Side.CLIENT)
			get() = 1F + (9F * remapRange(TerritoryRenderer.VOID_FACTOR_VALUE, (-0.5F)..(1F), (0F)..(1F)).coerceIn(0F, 1F).pow(1.5F))
		
		val currentRenderDistanceMp
			@Sided(Side.CLIENT)
			get() = MC.settings.renderDistanceChunks.let { if (it > 12) 0F else (1F - (it / 16.5F)).pow((it - 1) * 0.25F) }
		
		val DEFAULT_TEXTURE = Resource.Custom("textures/environment/white.png")
		val DEFAULT_COLOR = Vec3d(1.0, 1.0, 1.0)
		const val DEFAULT_ALPHA = 1F
		
		fun renderPlane(matrix: MatrixStack, y: Float, size: Float, rescale: Float){
			val mat = matrix.last.matrix
			
			TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX){
				pos(mat, -size, -y, -size).tex(0F, 0F).endVertex()
				pos(mat, -size, -y,  size).tex(0F, rescale).endVertex()
				pos(mat,  size, -y,  size).tex(rescale, rescale).endVertex()
				pos(mat,  size, -y, -size).tex(rescale, 0F).endVertex()
			}
		}
	}
	
	@Sided(Side.CLIENT)
	final override fun render(ticks: Int, partialTicks: Float, matrix: MatrixStack, world: ClientWorld, mc: Minecraft){
		GL.depthMask(false)
		RenderHelper.disableStandardItemLighting()
		render(world, matrix, partialTicks)
		GL.depthMask(true)
	}
	
	@Sided(Side.CLIENT)
	abstract fun render(world: ClientWorld, matrix: MatrixStack, partialTicks: Float)
}
