package chylex.hee.game.render.block
import chylex.hee.game.render.util.GL
import chylex.hee.system.Resource
import chylex.hee.system.util.square
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager.DestFactor
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.SourceFactor
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.TexGen.Q
import net.minecraft.client.renderer.GlStateManager.TexGen.R
import net.minecraft.client.renderer.GlStateManager.TexGen.S
import net.minecraft.client.renderer.GlStateManager.TexGen.T
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.GL_EYE_LINEAR
import org.lwjgl.opengl.GL11.GL_EYE_PLANE
import org.lwjgl.opengl.GL11.GL_MODELVIEW
import org.lwjgl.opengl.GL11.GL_OBJECT_LINEAR
import org.lwjgl.opengl.GL11.GL_OBJECT_PLANE
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_TEXTURE
import java.nio.FloatBuffer

@SideOnly(Side.CLIENT)
abstract class RenderTileAbstractPortal<T : TileEntity> : TileEntitySpecialRenderer<T>(){
	private companion object{
		@JvmStatic private val TEX_BACKGROUND = Resource.Vanilla("textures/environment/end_sky.png")
		@JvmStatic private val TEX_PARTICLE_LAYER = Resource.Vanilla("textures/entity/end_portal.png")
		
		@JvmStatic private val BUFFER = GLAllocation.createDirectFloatBuffer(16)
		
		private fun updateBuffer(value1: Float, value2: Float, value3: Float, value4: Float): FloatBuffer = BUFFER.apply {
			clear()
			put(value1)
			put(value2)
			put(value3)
			put(value4)
			flip()
		}
		
		private fun getLayerCount(distSq: Double): Int = when{
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
	private var globalTranslation = 0F
	
	// Properties
	
	protected val color = FloatArray(3)
	
	protected abstract fun generateNextColor(layer: Int)
	
	// Rendering
	
	override fun render(tile: T, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		cameraTarget = ActiveRenderInfo.getCameraPosition()
		globalTranslation = (Minecraft.getSystemTime() % 700000L) / 700000F
		
		val entityRenderer = Minecraft.getMinecraft().entityRenderer
		
		val offsetY = -y - 0.75
		val topY = offsetY + cameraTarget.y
		
		// setup
		
		GL.disableLighting()
		GL.enableBlend()
		
		GL.enableTexGenCoord(S)
		GL.enableTexGenCoord(T)
		GL.enableTexGenCoord(R)
		GL.enableTexGenCoord(Q)
		
		entityRenderer.setupFogColor(true)
		
		// background
		
		GL.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
		
		generateNextColor(0)
		transformColor { 0.1F }
		
		renderLayer(
			x, y, z,
			texture        = TEX_BACKGROUND,
			layerPosition  = (topY / (topY + 65.0)) - offsetY,
			layerRotation  = 0F,
			layerScale     = 0.125F,
			cameraOffsetMp = 65.0 / topY
		)
		
		// inner layers
		
		GL.blendFunc(SourceFactor.ONE, DestFactor.ONE)
		
		val layerCount = getLayerCount((x * x) + (y * y) + (z * z))
		
		for(layer in 1..15){
			val layerIndexRev = 16 - layer
			val colorMultiplier = 1F / (layerIndexRev + 1F)
			
			generateNextColor(layer)
			transformColor { it * colorMultiplier }
			
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
		
		entityRenderer.setupFogColor(false)
		
		GL.disableTexGenCoord(S)
		GL.disableTexGenCoord(T)
		GL.disableTexGenCoord(R)
		GL.disableTexGenCoord(Q)
		
		GL.disableBlend()
		GL.enableLighting()
	}
	
	// Utilities
	
	private inline fun transformColor(func: (Float) -> Float){
		color[0] = func(color[0])
		color[1] = func(color[1])
		color[2] = func(color[2])
	}
	
	private inline fun renderLayer(renderX: Double, renderY: Double, renderZ: Double, texture: ResourceLocation, layerPosition: Double, layerRotation: Float, layerScale: Float, cameraOffsetMp: Double){
		val globalX = rendererDispatcher.entityX
		val globalY = rendererDispatcher.entityY
		val globalZ = rendererDispatcher.entityZ
		
		// texture
		
		if (texture != TEX_BACKGROUND){
			bindTexture(TEX_BACKGROUND) // force re-bind to fix a bug in older AMD drivers
		}
		
		bindTexture(texture)
		
		GL.pushMatrix()
		GL.translate(globalX, layerPosition, globalZ)
		
		GL.texGen(S, GL_OBJECT_LINEAR)
		GL.texGen(T, GL_OBJECT_LINEAR)
		GL.texGen(R, GL_OBJECT_LINEAR)
		GL.texGen(Q, GL_EYE_LINEAR)
		
		GL.texGen(S, GL_OBJECT_PLANE, updateBuffer(1F, 0F, 0F, 0F))
		GL.texGen(T, GL_OBJECT_PLANE, updateBuffer(0F, 0F, 1F, 0F))
		GL.texGen(R, GL_OBJECT_PLANE, updateBuffer(1F, 0F, 0F, 1F))
		GL.texGen(Q, GL_EYE_PLANE,    updateBuffer(0F, 1F, 0F, 0F))
		
		GL.popMatrix()
		
		// position
		
		GL.matrixMode(GL_TEXTURE)
		GL.pushMatrix()
		GL.loadIdentity()
		
		GL.translate(0F, globalTranslation, 0F)
		GL.scale(layerScale, layerScale, layerScale)
		GL.translate(0.5F, 0.5F, 0.5F)
		GL.rotate(layerRotation, 0F, 0F, 1F)
		GL.translate((cameraTarget.x * cameraOffsetMp) - globalX - 0.5, (cameraTarget.z * cameraOffsetMp) - globalZ - 0.5, -globalY * 2)
		
		// rendering
		
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		
		buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
		buffer.pos(renderX + 0.0, renderY + 0.75, renderZ + 0.0).color(color[0], color[1], color[2], 1F).endVertex()
		buffer.pos(renderX + 0.0, renderY + 0.75, renderZ + 1.0).color(color[0], color[1], color[2], 1F).endVertex()
		buffer.pos(renderX + 1.0, renderY + 0.75, renderZ + 1.0).color(color[0], color[1], color[2], 1F).endVertex()
		buffer.pos(renderX + 1.0, renderY + 0.75, renderZ + 0.0).color(color[0], color[1], color[2], 1F).endVertex()
		tessellator.draw()
		
		GL.popMatrix()
		GL.matrixMode(GL_MODELVIEW)
	}
}
