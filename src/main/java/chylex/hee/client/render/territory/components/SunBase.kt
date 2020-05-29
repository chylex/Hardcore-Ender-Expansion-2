package chylex.hee.client.render.territory.components
import chylex.hee.client.render.territory.AbstractEnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.GL_GREATER

abstract class SunBase : AbstractEnvironmentRenderer(){
	protected companion object{
		const val DEFAULT_DISTANCE = 100.0
	}
	
	protected abstract val texture: ResourceLocation
	protected open val color = DEFAULT_COLOR
	protected open val alpha = DEFAULT_ALPHA
	protected abstract val size: Double
	protected open val distance = DEFAULT_DISTANCE
	
	@Sided(Side.CLIENT)
	protected open fun setRotation(world: ClientWorld, partialTicks: Float){
		GL.rotate(-90F, 0F, 1F, 0F)
		GL.rotate(world.getCelestialAngle(partialTicks) * 360F, 1F, 0F, 0F)
	}
	
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, partialTicks: Float){
		val width = size
		val dist = distance
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		GL.disableFog()
		
		GL.color(color, alpha)
		MC.textureManager.bindTexture(texture)
		
		GL.pushMatrix()
		setRotation(world, partialTicks)
		renderPlane(dist, width, 1.0)
		GL.popMatrix()
		
		GL.enableFog()
		GL.disableAlpha()
		GL.disableBlend()
	}
}
