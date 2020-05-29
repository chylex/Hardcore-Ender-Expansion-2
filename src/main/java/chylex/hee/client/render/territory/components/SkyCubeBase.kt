package chylex.hee.client.render.territory.components
import chylex.hee.client.render.territory.AbstractEnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.world.ClientWorld
import org.lwjgl.opengl.GL11.GL_GREATER

abstract class SkyCubeBase : AbstractEnvironmentRenderer(){
	protected companion object{
		const val DEFAULT_RESCALE = 16.0
		const val DEFAULT_DISTANCE = 125.0
	}
	
	protected open val texture = DEFAULT_TEXTURE
	protected open val color = DEFAULT_COLOR
	protected open val alpha = DEFAULT_ALPHA
	protected open val rescale = DEFAULT_RESCALE
	protected open val distance = DEFAULT_DISTANCE
	
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, partialTicks: Float){
		val distance = distance.coerceAtMost(18.5 * MC.settings.renderDistanceChunks)
		val rescale = rescale
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		
		GL.color(color, alpha * currentSkyAlpha)
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
			
			renderPlane(distance, distance, rescale)
			GL.popMatrix()
		}
		
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
