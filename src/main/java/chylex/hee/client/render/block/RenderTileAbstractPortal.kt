package chylex.hee.client.render.block

import chylex.hee.client.MC
import chylex.hee.client.render.gl.DF_ONE
import chylex.hee.client.render.gl.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.gl.RenderStateBuilder
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.FOG_ENABLED
import chylex.hee.client.render.gl.SF_SRC_ALPHA
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.BlockAbstractPortal.IPortalController
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.world.center
import chylex.hee.game.world.getTile
import chylex.hee.game.world.offsetWhile
import chylex.hee.system.facades.Facing4
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.square
import chylex.hee.system.math.toRadians
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.Matrix4f
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Sided(Side.CLIENT)
abstract class RenderTileAbstractPortal<T : TileEntityPortalInner, C : IPortalController>(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<T>(dispatcher) {
	private companion object {
		private val TEX_BACKGROUND = Resource.Vanilla("textures/environment/end_sky.png")
		private val TEX_PARTICLE_LAYER = Resource.Custom("textures/entity/portal.png")
		
		private val RENDER_TYPE_BACKGROUND = with(RenderStateBuilder()) {
			tex(TEX_BACKGROUND)
			fog(FOG_ENABLED)
			blend(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
			buildType("hee:portal_background_bottom", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, bufferSize = 256)
		}
		
		private val RENDER_TYPE_LAYER = Array(16) {
			with(RenderStateBuilder()) {
				tex(TEX_PARTICLE_LAYER)
				fog(FOG_ENABLED)
				blend(SF_SRC_ALPHA, DF_ONE)
				buildType("hee:portal_layer_${it}", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, bufferSize = 256)
			}
		}
		
		private fun getLayerCount(distSq: Double) = when {
			distSq > square(60) ->  5
			distSq > square(48) ->  7
			distSq > square(38) ->  9
			distSq > square(30) -> 11
			distSq > square(24) -> 13
			distSq > square(20) -> 14
			else                -> 15
		}
	}
	
	private var globalTranslation = 0.0
	
	private var isAnimating = false
	private var animationProgress = 0F
	
	private fun calculateEasing(layer: Int): Float {
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
	
	override fun render(tile: T, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int) {
		val world = tile.world ?: return
		val pos = tile.pos
		val mat = matrix.last.matrix
		
		var dist = -1
		
		for(facing in Facing4) {
			val testPos = pos.offsetWhile(facing, 1 until BlockAbstractPortal.MAX_SIZE) { it.getTile<TileEntityPortalInner>(world) != null }
			val testDist = abs((testPos.x - pos.x) + (testPos.z - pos.z))
			
			if (dist == -1) {
				dist = testDist
			}
			else if (dist != testDist) {
				return
			}
		}
		
		// setup
		
		val controller = findController(world, pos)
		rand.setSeed(controller?.let(::generateSeed) ?: 0L)
		
		animationProgress = controller?.clientAnimationProgress?.get(partialTicks) ?: 0F
		isAnimating = animationProgress > 0F && animationProgress < 1F
		
		globalTranslation = (MC.systemTime * BlockAbstractPortal.TRANSLATION_SPEED_INV) - (controller?.clientPortalOffset?.get(partialTicks) ?: 0F)
		
		// TODO fix bobbing
		
		val diff = renderDispatcher.renderInfo.projectedView.subtract(tile.pos.center)
		
		// background
		
		controller?.let { generateNextColor(it, 0) }
		transformColor { 0.1F } // discards provided value
		
		val background = buffer.getBuffer(RENDER_TYPE_BACKGROUND)
		renderBackgroundBottom(mat, background, dist)
		renderBackgroundSides(mat, background, dist)
		
		// inner layers
		
		val (x, y, z) = diff
		val layerCount = getLayerCount((x * x) + (y * y) + (z * z))
		
		for(layer in 1..15) {
			val layerIndexRev = 16 - layer
			val colorMultiplier = 1F / (layerIndexRev + 1F)
			
			controller?.let { generateNextColor(it, layer) }
			transformColor { it * colorMultiplier * calculateEasing(layer) }
			
			if (layerIndexRev <= layerCount) {
				renderLayer(mat, buffer.getBuffer(RENDER_TYPE_LAYER[layer - 1]), layer, dist, diff)
			}
		}
		
		// POLISH improve infinite illusion near edges
	}
	
	// Utilities
	
	private inline fun transformColor(func: (Float) -> Float) {
		color[0] = func(color[0])
		color[1] = func(color[1])
		color[2] = func(color[2])
	}
	
	private fun renderBackgroundBottom(mat: Matrix4f, builder: IVertexBuilder, dist: Int) {
		val sizePT = 1F + dist
		val sizeNT = -sizePT + 1F
		val yB = 0.02F
		val texW = 10F
		
		builder.pos(mat, sizeNT, yB, sizeNT).color().tex(0F, 0F).endVertex()
		builder.pos(mat, sizeNT, yB, sizePT).color().tex(0F, texW).endVertex()
		builder.pos(mat, sizePT, yB, sizePT).color().tex(texW, texW).endVertex()
		builder.pos(mat, sizePT, yB, sizeNT).color().tex(texW, 0F).endVertex()
	}
	
	private fun renderBackgroundSides(mat: Matrix4f, builder: IVertexBuilder, dist: Int) {
		val sizePT = 1F + dist
		val sizePB = sizePT - 0.01F
		val sizeNT = -sizePT + 1F
		val sizeNB = -sizePT + 1.01F
		val yT = 0.76F
		val yB = 0.02F
		
		val texW = 10F
		val texH = texW * (yT - yB) / (1F + (dist * 2))
		
		builder.pos(mat, sizeNB, yB, sizeNB).color().tex(0F, 0F).endVertex()
		builder.pos(mat, sizeNT, yT, sizeNT).color().tex(0F, texH).endVertex()
		builder.pos(mat, sizeNT, yT, sizePT).color().tex(texW, texH).endVertex()
		builder.pos(mat, sizeNB, yB, sizePB).color().tex(texW, 0F).endVertex()
		
		builder.pos(mat, sizeNB, yB, sizePB).color().tex(0F, 0F).endVertex()
		builder.pos(mat, sizeNT, yT, sizePT).color().tex(0F, texH).endVertex()
		builder.pos(mat, sizePT, yT, sizePT).color().tex(texW, texH).endVertex()
		builder.pos(mat, sizePB, yB, sizePB).color().tex(texW, 0F).endVertex()
		
		builder.pos(mat, sizePB, yB, sizePB).color().tex(0F, 0F).endVertex()
		builder.pos(mat, sizePT, yT, sizePT).color().tex(0F, texH).endVertex()
		builder.pos(mat, sizePT, yT, sizeNT).color().tex(texW, texH).endVertex()
		builder.pos(mat, sizePB, yB, sizeNB).color().tex(texW, 0F).endVertex()
		
		builder.pos(mat, sizePB, yB, sizeNB).color().tex(0F, 0F).endVertex()
		builder.pos(mat, sizePT, yT, sizeNT).color().tex(0F, texH).endVertex()
		builder.pos(mat, sizeNT, yT, sizeNT).color().tex(texW, texH).endVertex()
		builder.pos(mat, sizeNB, yB, sizeNB).color().tex(texW, 0F).endVertex()
	}
	
	private fun renderLayer(mat: Matrix4f, builder: IVertexBuilder, layer: Int, dist: Int, diff: Vec3d) {
		val layerIndexRev = 16 - layer
		val parallaxMp = (1F + abs(diff.y.toFloat() / 32F)).pow(0.12F)
		
		val layerOffset = globalTranslation.toFloat() * layer.toFloat().pow(1.3F) * 0.035F
		val rotationRad = (2F * ((square(layer) * 4321) + (layer * 9))).toRadians()
		val scaleShift = -0.325F + ((layerIndexRev.toFloat().pow(1.7F) - 1F) * 0.008F)
		val scaleBase = if (dist >= 2) 0.5F else 0.45F
		
		val x1 = 0.5F - scaleBase + (layer * 0.761F) - scaleShift
		val x2 = 0.5F + scaleBase + (layer * 0.761F) + scaleShift
		val y1 = 0.5F - scaleBase + (layer * 0.143F) - scaleShift + layerOffset
		val y2 = 0.5F + scaleBase + (layer * 0.143F) + scaleShift + layerOffset
		
		val cx = (x1 + x2) * 0.5F
		val cy = (y1 + y2) * 0.5F
		
		val sizeP = 1F + dist
		val sizeN = -sizeP + 1F
		val yT = 0.04F + ((layer - 1) * 0.044F)
		
		val parallaxX = -diff.x.toFloat() * layerIndexRev * 0.02F * sqrt(parallaxMp)
		val parallaxZ = -diff.z.toFloat() * layerIndexRev * 0.02F * sqrt(parallaxMp)
		
		val rotCos = cos(rotationRad)
		val rotSin = sin(rotationRad)
		
		builder.pos(mat, sizeN, yT, sizeN).color().tex(x1, y1, cx, cy, parallaxX, parallaxZ, rotCos, rotSin).endVertex()
		builder.pos(mat, sizeN, yT, sizeP).color().tex(x1, y2, cx, cy, parallaxX, parallaxZ, rotCos, rotSin).endVertex()
		builder.pos(mat, sizeP, yT, sizeP).color().tex(x2, y2, cx, cy, parallaxX, parallaxZ, rotCos, rotSin).endVertex()
		builder.pos(mat, sizeP, yT, sizeN).color().tex(x2, y1, cx, cy, parallaxX, parallaxZ, rotCos, rotSin).endVertex()
	}
	
	private fun IVertexBuilder.color(): IVertexBuilder {
		return this.color(color[0], color[1], color[2], 1F)
	}
	
	private fun IVertexBuilder.tex(x: Float, y: Float, cx: Float, cy: Float, ox: Float, oy: Float, rotCos: Float, rotSin: Float): IVertexBuilder {
		return this.tex(
			cx + (rotCos * (ox + x - cx)) - (rotSin * (oy + y - cy)),
			cy + (rotSin * (ox + x - cx)) + (rotCos * (oy + y - cy))
		)
	}
}
