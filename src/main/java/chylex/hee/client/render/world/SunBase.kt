package chylex.hee.client.render.world

import chylex.hee.client.render.util.DF_ONE
import chylex.hee.client.render.util.DF_ZERO
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.SF_ONE
import chylex.hee.client.render.util.SF_SRC_ALPHA
import chylex.hee.client.render.util.rotateX
import chylex.hee.client.render.util.rotateY
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.GL_GREATER

abstract class SunBase : AbstractEnvironmentRenderer() {
	protected companion object {
		const val DEFAULT_DISTANCE = 100F
	}
	
	protected abstract val texture: ResourceLocation
	protected open val color = DEFAULT_COLOR
	protected open val alpha = DEFAULT_ALPHA
	protected abstract val size: Float
	protected open val distance = DEFAULT_DISTANCE
	
	@Sided(Side.CLIENT)
	protected open fun setRotation(world: ClientWorld, matrix: MatrixStack, partialTicks: Float) {
		matrix.rotateY(-90F)
		matrix.rotateX(world.func_242415_f(partialTicks) * 360F)
	}
	
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, matrix: MatrixStack, partialTicks: Float) {
		val width = size
		val dist = distance
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		
		GL.color(color, alpha)
		GL.bindTexture(texture)
		
		matrix.push()
		setRotation(world, matrix, partialTicks)
		renderPlane(matrix, dist, width, 1F)
		matrix.pop()
		
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
