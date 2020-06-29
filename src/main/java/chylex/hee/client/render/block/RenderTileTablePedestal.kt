package chylex.hee.client.render.block
import chylex.hee.client.model.ModelHelper
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.RenderStateBuilder
import chylex.hee.client.render.util.RenderStateBuilder.Companion.ALPHA_NONE
import chylex.hee.client.render.util.RenderStateBuilder.Companion.CULL_DISABLED
import chylex.hee.client.render.util.RenderStateBuilder.Companion.LIGHTING_ENABLED
import chylex.hee.client.render.util.RenderStateBuilder.Companion.MASK_COLOR
import chylex.hee.client.render.util.rotateY
import chylex.hee.client.render.util.translateZ
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.size
import chylex.hee.system.util.toRadians
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.Matrix4f
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import java.util.Collections
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

@Sided(Side.CLIENT)
class RenderTileTablePedestal(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<TileEntityTablePedestal>(dispatcher){
	private companion object{
		private val RAND = Random()
		
		private val RENDER_TYPE_SHADOW = with(RenderStateBuilder()){
			tex(Resource.Vanilla("textures/misc/shadow.png"))
			blend(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
			lighting(LIGHTING_ENABLED)
			alpha(ALPHA_NONE)
			cull(CULL_DISABLED)
			mask(MASK_COLOR)
			buildType("hee:table_pedestal_shadow", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, bufferSize = 256)
		}
		
		private const val SHADOW_XZ_MIN = 0.325F
		private const val SHADOW_XZ_MAX = 0.675F
		private val SHADOW_Y = (BlockTablePedestal.COMBINED_BOX.maxY + 0.0015625).toFloat() // between the top of the pedestal and the status indicator
		
		private const val SPREAD_DEPTH_PER_2D_MODEL = 0.09375
		private const val SPREAD_RAND_2D = 0.13125
		private const val SPREAD_RAND_3D_XZ = 0.2625
		private const val SPREAD_RAND_3D_Y = 0.39375
		
		private val ITEM_ANGLES = (1..9).run {
			val section = 360F / endInclusive
			map { (it - 0.5F) * section }
		}
		
		private fun getItemModelCount(stackSize: Int) = when{
			stackSize > 48 -> 5
			stackSize > 32 -> 4
			stackSize > 16 -> 3
			stackSize >  1 -> 2
			else           -> 1
		}
	}
	
	override fun render(tile: TileEntityTablePedestal, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
		val itemRenderer = MC.itemRenderer
		
		val pos = tile.pos
		val stacks = tile.stacksForRendering
		
		val itemRotation = (MC.systemTime % 360000L) / 20F
		val baseSeed = pos.toLong()
		
		val offsetAngleIndices = if (stacks.size <= 1)
			Collections.emptyList()
		else
			ITEM_ANGLES.toMutableList()
		
		val shadowAlpha = if (MC.settings.entityShadows)
			(0.4 * (1.0 - (MC.renderManager.getDistanceToCamera(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) / 320.0))).toFloat().coerceIn(0F, 1F)
		else
			0F
		
		for((index, stack) in stacks.withIndex()){
			renderItemStack(matrix, buffer, itemRenderer, stack, index, itemRotation, baseSeed, offsetAngleIndices, shadowAlpha, combinedLight)
		}
	}
	
	private fun renderItemStack(matrix: MatrixStack, buffer: IRenderTypeBuffer, renderer: ItemRenderer, stack: ItemStack, index: Int, baseRotation: Float, baseSeed: Long, offsetAngleIndices: MutableList<Float>, shadowAlpha: Float, combinedLight: Int){
		matrix.push()
		
		var offsetY = 0F
		var rotationMp = 1F
		
		if (index > 0 && offsetAngleIndices.isNotEmpty()){
			val seed = baseSeed + (Item.getIdFromItem(stack.item) xor (33867 shl index))
			RAND.setSeed(seed)
			
			val locDistance = RAND.nextFloat(0.26, 0.29)
			val locIndex = RAND.nextInt(offsetAngleIndices.size)
			val locAngle = (offsetAngleIndices.removeAt(locIndex) + RAND.nextFloat(-3F, 3F)).toRadians()
			
			matrix.translate(cos(locAngle) * locDistance, 0.0, sin(locAngle) * locDistance)
			
			offsetY = RAND.nextFloat(0F, 0.05F)
			rotationMp = RAND.nextFloat(0.4F, 1.2F)
		}
		
		if (shadowAlpha > 0F){
			renderShadow(buffer, matrix.last.matrix, shadowAlpha)
		}
		
		val baseModel = ModelHelper.getItemModel(stack)
		val isModel3D = baseModel.isGui3d
		
		val baseY = if (isModel3D) 0.8325 else 1.0
		
		matrix.translate(0.5, baseY + offsetY, 0.5)
		matrix.rotateY(baseRotation * rotationMp)
		renderItemWithSpread(matrix, buffer, renderer, stack, baseModel, isModel3D, combinedLight)
		
		matrix.pop()
	}
	
	private fun renderItemWithSpread(matrix: MatrixStack, buffer: IRenderTypeBuffer, renderer: ItemRenderer, stack: ItemStack, model: IBakedModel, isModel3D: Boolean, combinedLight: Int){
		val extraModels = getItemModelCount(stack.size) - 1
		
		if (extraModels > 0){
			RAND.setSeed(Item.getIdFromItem(stack.item).toLong())
			
			if (!isModel3D){
				matrix.translateZ(-SPREAD_DEPTH_PER_2D_MODEL * (extraModels / 2.0))
			}
		}
		
		renderer.renderItem(stack, GROUND, false, matrix, buffer, combinedLight, OverlayTexture.NO_OVERLAY, model)
		
		repeat(extraModels){
			matrix.push()
			
			if (isModel3D){
				matrix.translate(
					RAND.nextFloat(-SPREAD_RAND_3D_XZ, SPREAD_RAND_3D_XZ),
					RAND.nextFloat(-SPREAD_RAND_3D_Y, SPREAD_RAND_3D_Y),
					RAND.nextFloat(-SPREAD_RAND_3D_XZ, SPREAD_RAND_3D_XZ)
				)
			}
			else{
				matrix.translate(
					RAND.nextFloat(-SPREAD_RAND_2D, SPREAD_RAND_2D),
					RAND.nextFloat(-SPREAD_RAND_2D, SPREAD_RAND_2D),
					SPREAD_DEPTH_PER_2D_MODEL * (it + 1)
				)
			}
			
			renderer.renderItem(stack, GROUND, false, matrix, buffer, combinedLight, OverlayTexture.NO_OVERLAY, model)
			matrix.pop()
		}
	}
	
	private fun renderShadow(buffer: IRenderTypeBuffer, mat: Matrix4f, alpha: Float){
		with(buffer.getBuffer(RENDER_TYPE_SHADOW)){
			pos(mat, SHADOW_XZ_MIN, SHADOW_Y, SHADOW_XZ_MIN).color(1F, 1F, 1F, alpha).tex(0F, 0F).endVertex()
			pos(mat, SHADOW_XZ_MIN, SHADOW_Y, SHADOW_XZ_MAX).color(1F, 1F, 1F, alpha).tex(0F, 1F).endVertex()
			pos(mat, SHADOW_XZ_MAX, SHADOW_Y, SHADOW_XZ_MAX).color(1F, 1F, 1F, alpha).tex(1F, 1F).endVertex()
			pos(mat, SHADOW_XZ_MAX, SHADOW_Y, SHADOW_XZ_MIN).color(1F, 1F, 1F, alpha).tex(1F, 0F).endVertex()
		}
	}
}
