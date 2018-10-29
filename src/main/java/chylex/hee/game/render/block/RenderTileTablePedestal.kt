package chylex.hee.game.render.block
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.render.util.GL
import chylex.hee.system.Resource
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.square
import chylex.hee.system.util.toRadians
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
import net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.RenderItem
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.client.ForgeHooksClient
import org.lwjgl.opengl.GL11.GL_ALWAYS
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_QUADS
import java.util.Collections
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

object RenderTileTablePedestal : TileEntitySpecialRenderer<TileEntityTablePedestal>(){
	private val TEX_BLOCKS_ITEMS = TextureMap.LOCATION_BLOCKS_TEXTURE
	private val TEX_SHADOW = Resource.Vanilla("textures/misc/shadow.png")
	private val RAND = Random()
	
	private const val SHADOW_XZ_MIN = 0.325
	private const val SHADOW_XZ_MAX = 0.675
	private val SHADOW_Y = BlockTablePedestal.COMBINED_BOX.maxY + 0.0015625 // between the top of the pedestal and the status indicator
	
	private val ITEM_ANGLES = (1..10).run {
		val section = 360F / endInclusive
		map { (it - 0.5F) * section }
	}
	
	override fun render(tile: TileEntityTablePedestal, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		val pos = tile.pos
		val stacks = tile.stacksForRendering
		
		val mc = Minecraft.getMinecraft()
		val textureManager = mc.renderEngine
		val itemRenderer = mc.renderItem
		
		val texObj = textureManager.getTexture(TEX_BLOCKS_ITEMS).also { it.setBlurMipmap(false, false) }
		textureManager.bindTexture(TEX_BLOCKS_ITEMS)
		
		GL.pushMatrix()
		GL.translate(x, y, z)
		
		RenderHelper.enableStandardItemLighting()
		GL.alphaFunc(GL_GREATER, 0.1F)
		GL.enableRescaleNormal()
		GL.enableBlend()
		GL.tryBlendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)
		
		val itemRotation = (Minecraft.getSystemTime() % 360000L) / 20F
		val baseSeed = pos.toLong()
		
		val offsetAngleIndices = if (stacks.size <= 1)
			Collections.emptyList()
		else
			ITEM_ANGLES.toMutableList()
		
		val shadowAlpha = if (mc.gameSettings.entityShadows)
			(0.25 * world.getLightBrightness(pos) * (1.0 - (square(x) + square(y) + square(z)) / 256.0)).toFloat().coerceAtMost(1F)
		else
			0F
		
		for((index, stack) in stacks.withIndex()){
			renderItemStack(textureManager, itemRenderer, stack, index, itemRotation, baseSeed, offsetAngleIndices, shadowAlpha)
		}
		
		GL.disableBlend()
		GL.disableRescaleNormal()
		
		GL.popMatrix()
		texObj.restoreLastBlurMipmap()
	}
	
	private fun renderItemStack(textureManager: TextureManager, renderer: RenderItem, stack: ItemStack, index: Int, baseRotation: Float, baseSeed: Long, offsetAngleIndices: MutableList<Float>, shadowAlpha: Float){
		GL.pushMatrix()
		
		var offsetY = 0F
		var rotationMp = 1F
		
		if (index > 0 && offsetAngleIndices.isNotEmpty()){
			val seed = baseSeed + ((Item.getIdFromItem(stack.item) + stack.metadata) xor (33867 shl index))
			RAND.setSeed(seed)
			
			val locDistance = RAND.nextFloat(0.26F, 0.29F)
			val locIndex = RAND.nextInt(offsetAngleIndices.size)
			val locAngle = (offsetAngleIndices.removeAt(locIndex) + RAND.nextFloat(-3F, 3F)).toRadians()
			
			GL.translate(cos(locAngle) * locDistance, 0.0, sin(locAngle) * locDistance)
			
			offsetY = RAND.nextFloat(0F, 0.05F)
			rotationMp = RAND.nextFloat(0.4F, 1.2F)
		}
		
		if (shadowAlpha > 0F){
			GL.depthMask(false)
			GL.alphaFunc(GL_ALWAYS, 0F)
			textureManager.bindTexture(TEX_SHADOW)
			
			renderShadow(shadowAlpha)
			
			GL.depthMask(true)
			GL.alphaFunc(GL_GREATER, 0.1F)
			textureManager.bindTexture(TEX_BLOCKS_ITEMS)
		}
		
		val baseModel = renderer.getItemModelWithOverrides(stack, world, null)
		val baseY = if (baseModel.isGui3d) 0.8325F else 1F
		
		GL.translate(0.5F, baseY + offsetY, 0.5F)
		GL.rotate(baseRotation * rotationMp, 0F, 1F, 0F)
		renderer.renderItem(stack, ForgeHooksClient.handleCameraTransforms(baseModel, TransformType.GROUND, false))
		
		GL.popMatrix()
	}
	
	private fun renderShadow(alpha: Float){
		val tessellator = Tessellator.getInstance()
		val buffer = tessellator.buffer
		
		buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
		buffer.pos(SHADOW_XZ_MIN, SHADOW_Y, SHADOW_XZ_MIN).tex(0.0, 0.0).color(1F, 1F, 1F, alpha).endVertex()
		buffer.pos(SHADOW_XZ_MIN, SHADOW_Y, SHADOW_XZ_MAX).tex(0.0, 1.0).color(1F, 1F, 1F, alpha).endVertex()
		buffer.pos(SHADOW_XZ_MAX, SHADOW_Y, SHADOW_XZ_MAX).tex(1.0, 1.0).color(1F, 1F, 1F, alpha).endVertex()
		buffer.pos(SHADOW_XZ_MAX, SHADOW_Y, SHADOW_XZ_MIN).tex(1.0, 0.0).color(1F, 1F, 1F, alpha).endVertex()
		tessellator.draw()
	}
}
