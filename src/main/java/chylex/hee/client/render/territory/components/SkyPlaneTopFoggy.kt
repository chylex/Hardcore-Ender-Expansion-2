package chylex.hee.client.render.territory.components
import chylex.hee.client.render.territory.EnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.GL_GREATER

class SkyPlaneTopFoggy(
	override val texture: ResourceLocation,
	override val color: Vec3d,
	override val alpha: Float = DEFAULT_ALPHA,
	override val rescale: Double = DEFAULT_RESCALE,
	override val distance: Double = DEFAULT_DISTANCE,
	private val width: Double = distance
) : SkyCubeBase(){
	@Sided(Side.CLIENT)
	override fun render(ticks: Int, partialTicks: Float, world: ClientWorld, mc: Minecraft){
		val dist = distance.coerceAtMost(18.5 * mc.gameSettings.renderDistanceChunks)
		val rescale = rescale
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		RenderHelper.disableStandardItemLighting()
		
		GL.color(color, alpha * EnvironmentRenderer.currentSkyAlpha)
		MC.textureManager.bindTexture(texture)
		
		GL.pushMatrix()
		GL.rotate(180F, 1F, 0F, 0F)
		renderPlane(dist, width, rescale)
		GL.popMatrix()
		
		GL.disableAlpha()
		GL.disableBlend()
	}
}
