package chylex.hee.client.render.world

import chylex.hee.client.render.util.GL
import chylex.hee.game.Resource
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Vec3
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.world.ClientWorld
import net.minecraftforge.client.ISkyRenderHandler
import org.lwjgl.opengl.GL11.GL_QUADS

abstract class AbstractEnvironmentRenderer : ISkyRenderHandler {
	companion object {
		val DEFAULT_TEXTURE = Resource.Custom("textures/environment/white.png")
		val DEFAULT_COLOR = Vec3.xyz(1.0)
		const val DEFAULT_ALPHA = 1F
		
		fun renderPlane(matrix: MatrixStack, y: Float, size: Float, rescale: Float) {
			val mat = matrix.last.matrix
			
			with(Tessellator.getInstance()) {
				buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
				buffer.pos(mat, -size, -y, -size).tex(0F, 0F).endVertex()
				buffer.pos(mat, -size, -y,  size).tex(0F, rescale).endVertex()
				buffer.pos(mat,  size, -y,  size).tex(rescale, rescale).endVertex()
				buffer.pos(mat,  size, -y, -size).tex(rescale, 0F).endVertex()
				draw()
			}
		}
	}
	
	@Sided(Side.CLIENT)
	final override fun render(ticks: Int, partialTicks: Float, matrix: MatrixStack, world: ClientWorld, mc: Minecraft) {
		GL.depthMask(false)
		RenderHelper.disableStandardItemLighting()
		render(world, matrix, partialTicks)
		GL.depthMask(true)
	}
	
	@Sided(Side.CLIENT)
	abstract fun render(world: ClientWorld, matrix: MatrixStack, partialTicks: Float)
}
