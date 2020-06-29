package chylex.hee.client.render.territory.components
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.rotateX
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.GL_GREATER

class SkyPlaneTopFoggy(
	override val texture: ResourceLocation = DEFAULT_TEXTURE,
	override val color: Vec3d = DEFAULT_COLOR,
	override val alpha: Float = DEFAULT_ALPHA,
	override val rescale: Float = DEFAULT_RESCALE,
	override val distance: Float = DEFAULT_DISTANCE,
	private val width: Float = distance
) : SkyCubeBase(){
	@Sided(Side.CLIENT)
	override fun render(world: ClientWorld, matrix: MatrixStack, partialTicks: Float){
		val dist = distance.coerceAtMost(18.5F * MC.settings.renderDistanceChunks)
		val rescale = rescale
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
		GL.enableAlpha()
		GL.alphaFunc(GL_GREATER, 0F)
		
		GL.color(color, alpha * currentSkyAlpha)
		GL.bindTexture(texture)
		
		matrix.push()
		matrix.rotateX(180F)
		renderPlane(matrix, dist, width, rescale)
		matrix.pop()
		
		GL.disableAlpha()
		GL.disableBlend()
	}
}
