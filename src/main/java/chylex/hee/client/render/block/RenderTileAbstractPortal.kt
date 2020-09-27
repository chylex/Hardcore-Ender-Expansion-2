package chylex.hee.client.render.block
import chylex.hee.client.MC
import chylex.hee.client.render.gl.DF_ONE
import chylex.hee.client.render.gl.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.gl.GL
import chylex.hee.client.render.gl.RenderStateBuilder
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.FOG_ENABLED
import chylex.hee.client.render.gl.SF_ONE
import chylex.hee.client.render.gl.SF_SRC_ALPHA
import chylex.hee.client.render.gl.TEX_Q
import chylex.hee.client.render.gl.TEX_R
import chylex.hee.client.render.gl.TEX_S
import chylex.hee.client.render.gl.TEX_T
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockAbstractPortal.IPortalController
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.world.center
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.square
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.Matrix4f
import net.minecraft.client.renderer.RenderState.TexturingState
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_EYE_LINEAR
import org.lwjgl.opengl.GL11.GL_EYE_PLANE
import org.lwjgl.opengl.GL11.GL_MODELVIEW
import org.lwjgl.opengl.GL11.GL_OBJECT_LINEAR
import org.lwjgl.opengl.GL11.GL_OBJECT_PLANE
import org.lwjgl.opengl.GL11.GL_TEXTURE
import java.util.Random
import kotlin.math.pow

@Sided(Side.CLIENT)
abstract class RenderTileAbstractPortal<T : TileEntityPortalInner, C : IPortalController>(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<T>(dispatcher){
	private companion object{
		private val TEX_BACKGROUND = Resource.Vanilla("textures/environment/end_sky.png")
		private val TEX_PARTICLE_LAYER = Resource.Vanilla("textures/entity/end_portal.png")
		
		// UPDATE figure out how to rewrite in the new rendering system
		
		private fun RENDER_TEXTURING_STATE(matrix: Matrix4f, layer: Int, globalTranslation: Double, y: Double): TexturingState{
			val cameraTarget = Vec3d.ZERO
			val layerRotation: Float
			val layerScale: Float
			
			if (layer == 0){
				layerRotation = 0F
				layerScale = 0.125F
			}
			else{
				layerRotation = 2F * ((square(layer) * 4321) + (layer * 9))
				layerScale = 0.0625F
			}
			
			return object : TexturingState("hee:portal_layer_$layer", {
				val offsetY = -y - 0.75
				val topY = offsetY + cameraTarget.y
				
				val layerPosition: Double
				val cameraOffsetMp: Double
				
				if (layer == 0){
					layerPosition  = (topY / (topY + 65.0)) - offsetY
					cameraOffsetMp = 65.0 / topY
				}
				else{
					val layerIndexRev = 16 - layer
					layerPosition  = (topY / (topY + layerIndexRev)) - offsetY
					cameraOffsetMp = layerIndexRev / topY
				}
				
				val globalX = MC.player!!.posX % 69420.0 // POLISH works around extreme coordinates, but causes a sudden jump at boundary
				val globalY = MC.player!!.posY
				val globalZ = MC.player!!.posZ % 69420.0
				
				GL.matrixMode(GL_MODELVIEW)
				GL.pushMatrix()
				GL.translate(globalX, layerPosition, globalZ)
				
				GL.enableTexGenCoord(TEX_S)
				GL.enableTexGenCoord(TEX_T)
				GL.enableTexGenCoord(TEX_R)
				GL.enableTexGenCoord(TEX_Q)
				
				GL.texGenMode(TEX_S, GL_OBJECT_LINEAR)
				GL.texGenMode(TEX_T, GL_OBJECT_LINEAR)
				GL.texGenMode(TEX_R, GL_OBJECT_LINEAR)
				GL.texGenMode(TEX_Q, GL_EYE_LINEAR)
				
				GL.texGenParam(TEX_S, GL_OBJECT_PLANE, updateBuffer(1F, 0F, 0F, 0F))
				GL.texGenParam(TEX_T, GL_OBJECT_PLANE, updateBuffer(0F, 0F, 1F, 0F))
				GL.texGenParam(TEX_R, GL_OBJECT_PLANE, updateBuffer(1F, 0F, 0F, 1F))
				GL.texGenParam(TEX_Q, GL_EYE_PLANE, updateBuffer(0F, 1F, 0F, 0F))
				
				GL.popMatrix()
				
				GL.matrixMode(GL_TEXTURE)
				GL.pushMatrix()
				GL.loadIdentity()
				
				GL.translate(0.0, globalTranslation, 0.0)
				GL.scale(layerScale, layerScale, layerScale)
				GL.translate(0.5, 0.5, 0.5)
				GL.rotate(layerRotation, 0F, 0F, 1F)
				GL.translate((cameraTarget.x * cameraOffsetMp) - globalX - 0.5, (cameraTarget.z * cameraOffsetMp) - globalZ - 0.5, -globalY * 2)
			}, {
				GL.matrixMode(GL_TEXTURE)
				GL.popMatrix()
				GL.matrixMode(GL_MODELVIEW)
				
				GL.disableTexGenCoord(TEX_S)
				GL.disableTexGenCoord(TEX_T)
				GL.disableTexGenCoord(TEX_R)
				GL.disableTexGenCoord(TEX_Q)
			}){}
		}
		
		private fun RENDER_TYPE_BACKGROUND(mat: Matrix4f, globalTranslation: Double, y: Double) = with(RenderStateBuilder()){
			tex(TEX_BACKGROUND)
			fog(FOG_ENABLED)
			blend(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
			texturing(RENDER_TEXTURING_STATE(mat, 0, globalTranslation, y))
			buildType("hee:portal_background", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, bufferSize = 256)
		}
		
		private fun RENDER_TYPE_LAYERS(mat: Matrix4f, layer: Int, globalTranslation: Double, y: Double) = with(RenderStateBuilder()){
			tex(TEX_PARTICLE_LAYER)
			fog(FOG_ENABLED)
			blend(SF_ONE, DF_ONE)
			texturing(RENDER_TEXTURING_STATE(mat, layer, globalTranslation, y))
			buildType("hee:portal_layer_$layer", DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, bufferSize = 256)
		}
		
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
	
	override fun render(tile: T, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
		val controller = findController(tile.world ?: return, tile.pos)
		
		rand.setSeed(controller?.let { generateSeed(it) } ?: 0L)
		
		animationProgress = controller?.clientAnimationProgress?.get(partialTicks) ?: 0F
		isAnimating = animationProgress > 0F && animationProgress < 1F
		
		cameraTarget = Vec3d.ZERO // UPDATE fix bobbing
		globalTranslation = ((MC.systemTime % BlockAbstractPortal.TRANSLATION_SPEED_LONG) * BlockAbstractPortal.TRANSLATION_SPEED_INV) - (controller?.clientPortalOffset?.get(partialTicks) ?: 0F)
		
		val (x, y, z) = renderDispatcher.renderInfo.projectedView.subtract(tile.pos.center)
		val mat = matrix.last.matrix
		
		// background
		
		controller?.let { generateNextColor(it, 0) }
		transformColor { 0.1F } // discards provided value
		
		renderLayer(mat, buffer.getBuffer(RENDER_TYPE_BACKGROUND(mat, globalTranslation, y)))
		
		// inner layers
		
		val layerCount = getLayerCount((x * x) + (y * y) + (z * z))
		
		for(layer in 1..15){
			val layerIndexRev = 16 - layer
			val colorMultiplier = 1F / (layerIndexRev + 1F)
			
			controller?.let { generateNextColor(it, layer) }
			transformColor { it * colorMultiplier * calculateEasing(layer) }
			
			if (layerIndexRev <= layerCount){
				renderLayer(mat, buffer.getBuffer(RENDER_TYPE_LAYERS(mat, layer, globalTranslation, y)))
			}
		}
	}
	
	// Utilities
	
	private inline fun transformColor(func: (Float) -> Float){
		color[0] = func(color[0])
		color[1] = func(color[1])
		color[2] = func(color[2])
	}
	
	private fun renderLayer(mat: Matrix4f, builder: IVertexBuilder){
		builder.pos(mat, 0F, 0.75F, 0F).color(color[0], color[1], color[2], 1F).endVertex()
		builder.pos(mat, 0F, 0.75F, 1F).color(color[0], color[1], color[2], 1F).endVertex()
		builder.pos(mat, 1F, 0.75F, 1F).color(color[0], color[1], color[2], 1F).endVertex()
		builder.pos(mat, 1F, 0.75F, 0F).color(color[0], color[1], color[2], 1F).endVertex()
	}
}
