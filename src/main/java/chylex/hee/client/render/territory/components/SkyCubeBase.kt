package chylex.hee.client.render.territory.components
import chylex.hee.client.render.territory.AbstractEnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.rotateX
import chylex.hee.client.render.util.rotateZ
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.world.ClientWorld
import org.lwjgl.opengl.GL11.GL_GREATER

abstract class SkyCubeBase : AbstractEnvironmentRenderer(){
	protected companion object{
		const val DEFAULT_RESCALE = 16F
		const val DEFAULT_DISTANCE = 125F
	}
	
	protected open val texture = DEFAULT_TEXTURE
	protected open val color = DEFAULT_COLOR
	protected open val alpha = DEFAULT_ALPHA
	protected open val rescale = DEFAULT_RESCALE
	protected open val distance = DEFAULT_DISTANCE
	
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, matrix: MatrixStack, partialTicks: Float){
		val distance = distance.coerceAtMost(18.5F * MC.settings.renderDistanceChunks)
		val rescale = rescale
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		
		GL.color(color, alpha * currentSkyAlpha)
		GL.bindTexture(texture)
		
		for(side in 0..5){
			matrix.push()
			
			when(side){
				1 -> matrix.rotateX( 90F)
				2 -> matrix.rotateX(-90F)
				3 -> matrix.rotateX(180F)
				4 -> matrix.rotateZ( 90F)
				5 -> matrix.rotateZ(-90F)
			}
			
			renderPlane(matrix, distance, distance, rescale)
			matrix.pop()
		}
		
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
