package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.GL.TEX_Q
import chylex.hee.client.render.util.GL.TEX_R
import chylex.hee.client.render.util.GL.TEX_S
import chylex.hee.client.render.util.GL.TEX_T
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockAbstractPortal.IPortalController
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.square
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.lwjgl.opengl.GL11.GL_EYE_LINEAR
import org.lwjgl.opengl.GL11.GL_EYE_PLANE
import org.lwjgl.opengl.GL11.GL_MODELVIEW
import org.lwjgl.opengl.GL11.GL_OBJECT_LINEAR
import org.lwjgl.opengl.GL11.GL_OBJECT_PLANE
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_TEXTURE
import java.util.Random
import kotlin.math.pow

@Sided(Side.CLIENT)
abstract class RenderTileAbstractPortal<T : TileEntityPortalInner, C : IPortalController> : TileEntityRenderer<T>(){
	private companion object{
		private val TEX_BACKGROUND = Resource.Vanilla("textures/environment/end_sky.png")
		private val TEX_PARTICLE_LAYER = Resource.Vanilla("textures/entity/end_portal.png")
		
		private val BUFFER = GLAllocation.createDirectFloatBuffer(16)
		
		private fun updateBuffer(value1: Float, value2: Float, value3: Float, value4: Float) = BUFFER.apply {
			clear()
			put(value1)
			put(value2)
			put(value3)
			put(value4)
			flip()
		}
		
		private fun getLayerCount(distSq: Double) = when{
			distSq > square(60) ->  5
			distSq > square(48) ->  7
			distSq > square(38) ->  9
			distSq > square(30) -> 11
			distSq > square(24) -> 13
			distSq > square(20) -> 14
			else                -> 15
		}
	}
	
	private var cameraTarget = Vec3d.ZERO
	private var globalTranslation = 0.0
	
	private var isAnimating = false
	private var animationProgress = 0F
	
	private fun calculateEasing(layer: Int): Float{
		return if (isAnimating)
			(1.1F - square(animationProgress * 4.5F - 4.816F) + 22.1F * (1F - ((layer - 1F) / 14F).pow(1.2F))).coerceIn(0F, 1F).pow(1.5F)
		else
			animationProgress
	}
	
	// Properties
	
	protected val rand = Random()
	protected val color = FloatArray(3)
	
	protected abstract fun findController(world: World, pos: BlockPos): C?
	
	protected abstract fun generateSeed(controller: C): Long
	protected abstract fun generateNextColor(controller: C, layer: Int)
	
	// Rendering
	
	override fun render(tile: T, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int){
		val controller = findController(tile.world ?: return, tile.pos)
		
		rand.setSeed(controller?.let { generateSeed(it) } ?: 0L)
		
		animationProgress = controller?.clientAnimationProgress?.get(partialTicks) ?: 0F
		isAnimating = animationProgress > 0F && animationProgress < 1F
		
		cameraTarget = Vec3d.ZERO // UPDATE fix bobbing
		globalTranslation = ((MC.systemTime % BlockAbstractPortal.TRANSLATION_SPEED_LONG) * BlockAbstractPortal.TRANSLATION_SPEED_INV) - (controller?.clientPortalOffset?.get(partialTicks) ?: 0F)
		
		val offsetY = -y - 0.75
		val topY = offsetY + cameraTarget.y
		
		// setup
		
		GL.disableLighting()
		GL.enableBlend()
		
		GL.enableTexGenCoord(TEX_S)
		GL.enableTexGenCoord(TEX_T)
		GL.enableTexGenCoord(TEX_R)
		GL.enableTexGenCoord(TEX_Q)
		
		MC.gameRenderer.setupFogColor(true)
		
		// background
		
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
		
		controller?.let { generateNextColor(it, 0) }
		transformColor { 0.1F } // discards provided value
		
		renderLayer(
			x, y, z,
			texture        = TEX_BACKGROUND,
			layerPosition  = (topY / (topY + 65.0)) - offsetY,
			layerRotation  = 0F,
			layerScale     = 0.125F,
			cameraOffsetMp = 65.0 / topY
		)
		
		// inner layers
		
		GL.blendFunc(SF_ONE, DF_ONE)
		
		val layerCount = getLayerCount((x * x) + (y * y) + (z * z))
		
		for(layer in 1..15){
			val layerIndexRev = 16 - layer
			val colorMultiplier = 1F / (layerIndexRev + 1F)
			
			controller?.let { generateNextColor(it, layer) }
			transformColor { it * colorMultiplier * calculateEasing(layer) }
			
			if (layerIndexRev <= layerCount){
				renderLayer(
					x, y, z,
					texture        = TEX_PARTICLE_LAYER,
					layerPosition  = (topY / (topY + layerIndexRev)) - offsetY,
					layerRotation  = 2F * ((square(layer) * 4321) + (layer * 9)),
					layerScale     = 0.0625F,
					cameraOffsetMp = layerIndexRev / topY
				)
			}
		}
		
		// cleanup
		
		MC.gameRenderer.setupFogColor(false)
		
		GL.disableTexGenCoord(TEX_S)
		GL.disableTexGenCoord(TEX_T)
		GL.disableTexGenCoord(TEX_R)
		GL.disableTexGenCoord(TEX_Q)
		
		GL.disableBlend()
		GL.enableLighting()
	}
	
	// Utilities
	
	private inline fun transformColor(func: (Float) -> Float){
		color[0] = func(color[0])
		color[1] = func(color[1])
		color[2] = func(color[2])
	}
	
	private fun renderLayer(renderX: Double, renderY: Double, renderZ: Double, texture: ResourceLocation, layerPosition: Double, layerRotation: Float, layerScale: Float, cameraOffsetMp: Double){
		val globalX = TileEntityRendererDispatcher.staticPlayerX % 69420.0 // TODO works around extreme coordinates, but causes a sudden jump at boundary
		val globalY = TileEntityRendererDispatcher.staticPlayerY
		val globalZ = TileEntityRendererDispatcher.staticPlayerZ % 69420.0
		
		// texture
		
		if (texture != TEX_BACKGROUND){
			bindTexture(TEX_BACKGROUND) // force re-bind to fix a bug in older AMD drivers
		}
		
		bindTexture(texture)
		
		GL.pushMatrix()
		GL.translate(globalX, layerPosition, globalZ)
		
		GL.texGenMode(TEX_S, GL_OBJECT_LINEAR)
		GL.texGenMode(TEX_T, GL_OBJECT_LINEAR)
		GL.texGenMode(TEX_R, GL_OBJECT_LINEAR)
		GL.texGenMode(TEX_Q, GL_EYE_LINEAR)
		
		GL.texGenParam(TEX_S, GL_OBJECT_PLANE, updateBuffer(1F, 0F, 0F, 0F))
		GL.texGenParam(TEX_T, GL_OBJECT_PLANE, updateBuffer(0F, 0F, 1F, 0F))
		GL.texGenParam(TEX_R, GL_OBJECT_PLANE, updateBuffer(1F, 0F, 0F, 1F))
		GL.texGenParam(TEX_Q, GL_EYE_PLANE, updateBuffer(0F, 1F, 0F, 0F))
		
		GL.popMatrix()
		
		// position
		
		GL.matrixMode(GL_TEXTURE)
		GL.pushMatrix()
		GL.loadIdentity()
		
		GL.translate(0.0, globalTranslation, 0.0)
		GL.scale(layerScale, layerScale, layerScale)
		GL.translate(0.5F, 0.5F, 0.5F)
		GL.rotate(layerRotation, 0F, 0F, 1F)
		GL.translate((cameraTarget.x * cameraOffsetMp) - globalX - 0.5, (cameraTarget.z * cameraOffsetMp) - globalZ - 0.5, -globalY * 2)
		
		// rendering
		
		TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_COLOR){
			pos(renderX + 0.0, renderY + 0.75, renderZ + 0.0).color(color[0], color[1], color[2], 1F).endVertex()
			pos(renderX + 0.0, renderY + 0.75, renderZ + 1.0).color(color[0], color[1], color[2], 1F).endVertex()
			pos(renderX + 1.0, renderY + 0.75, renderZ + 1.0).color(color[0], color[1], color[2], 1F).endVertex()
			pos(renderX + 1.0, renderY + 0.75, renderZ + 0.0).color(color[0], color[1], color[2], 1F).endVertex()
		}
		
		GL.popMatrix()
		GL.matrixMode(GL_MODELVIEW)
	}
}
