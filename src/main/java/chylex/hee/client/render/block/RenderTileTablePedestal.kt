package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.ItemRenderHelper
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.size
import chylex.hee.system.util.toRadians
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
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

@Sided(Side.CLIENT)
object RenderTileTablePedestal : TileEntityRenderer<TileEntityTablePedestal>(){
	private val TEX_SHADOW = Resource.Vanilla("textures/misc/shadow.png")
	private val RAND = Random()
	
	private const val SHADOW_XZ_MIN = 0.325
	private const val SHADOW_XZ_MAX = 0.675
	private val SHADOW_Y = BlockTablePedestal.COMBINED_BOX.maxY + 0.0015625 // between the top of the pedestal and the status indicator
	
	private const val SPREAD_DEPTH_PER_2D_MODEL = 0.09375F
	private const val SPREAD_RAND_2D = 0.13125F
	private const val SPREAD_RAND_3D_XZ = 0.2625F
	private const val SPREAD_RAND_3D_Y = 0.39375F
	
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
	
	override fun render(tile: TileEntityTablePedestal, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int){
		val textureManager = MC.textureManager
		val itemRenderer = MC.itemRenderer
		
		ItemRenderHelper.beginItemModel()
		GL.pushMatrix()
		GL.translate(x, y, z)
		
		val pos = tile.pos
		val stacks = tile.stacksForRendering
		
		val itemRotation = (MC.systemTime % 360000L) / 20F
		val baseSeed = pos.toLong()
		
		val offsetAngleIndices = if (stacks.size <= 1)
			Collections.emptyList()
		else
			ITEM_ANGLES.toMutableList()
		
		val shadowAlpha = if (MC.settings.entityShadows)
			(0.75 * (1.0 - (MC.renderManager.getDistanceToCamera(x, y, z) / 256.0))).toFloat().coerceAtMost(1F)
		else
			0F
		
		for((index, stack) in stacks.withIndex()){
			renderItemStack(textureManager, itemRenderer, stack, index, itemRotation, baseSeed, offsetAngleIndices, shadowAlpha)
		}
		
		GL.popMatrix()
		ItemRenderHelper.endItemModel()
	}
	
	private fun renderItemStack(textureManager: TextureManager, renderer: ItemRenderer, stack: ItemStack, index: Int, baseRotation: Float, baseSeed: Long, offsetAngleIndices: MutableList<Float>, shadowAlpha: Float){
		GL.pushMatrix()
		
		var offsetY = 0F
		var rotationMp = 1F
		
		if (index > 0 && offsetAngleIndices.isNotEmpty()){
			val seed = baseSeed + (Item.getIdFromItem(stack.item) xor (33867 shl index))
			RAND.setSeed(seed)
			
			val locDistance = RAND.nextFloat(0.26F, 0.29F)
			val locIndex = RAND.nextInt(offsetAngleIndices.size)
			val locAngle = (offsetAngleIndices.removeAt(locIndex) + RAND.nextFloat(-3F, 3F)).toRadians()
			
			GL.translate(cos(locAngle) * locDistance, 0F, sin(locAngle) * locDistance)
			
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
			textureManager.bindTexture(ItemRenderHelper.TEX_BLOCKS_ITEMS)
		}
		
		val baseModel = ItemRenderHelper.getItemModel(stack)
		val isModel3D = baseModel.isGui3d
		
		val baseY = if (isModel3D) 0.8325F else 1F
		
		GL.translate(0.5F, baseY + offsetY, 0.5F)
		GL.rotate(baseRotation * rotationMp, 0F, 1F, 0F)
		renderItemWithSpread(renderer, stack, ForgeHooksClient.handleCameraTransforms(baseModel, GROUND, false), isModel3D)
		
		GL.popMatrix()
	}
	
	private fun renderItemWithSpread(renderer: ItemRenderer, stack: ItemStack, model: IBakedModel, isModel3D: Boolean){
		val extraModels = getItemModelCount(stack.size) - 1
		
		if (extraModels > 0){
			RAND.setSeed(Item.getIdFromItem(stack.item).toLong())
			
			if (!isModel3D){
				GL.translate(0F, 0F, -SPREAD_DEPTH_PER_2D_MODEL * (extraModels / 2F))
			}
		}
		
		renderer.renderItem(stack, model)
		
		repeat(extraModels){
			GL.pushMatrix()
			
			if (isModel3D){
				GL.translate(
					RAND.nextFloat(-SPREAD_RAND_3D_XZ, SPREAD_RAND_3D_XZ),
					RAND.nextFloat(-SPREAD_RAND_3D_Y, SPREAD_RAND_3D_Y),
					RAND.nextFloat(-SPREAD_RAND_3D_XZ, SPREAD_RAND_3D_XZ)
				)
			}
			else{
				GL.translate(
					RAND.nextFloat(-SPREAD_RAND_2D, SPREAD_RAND_2D),
					RAND.nextFloat(-SPREAD_RAND_2D, SPREAD_RAND_2D),
					SPREAD_DEPTH_PER_2D_MODEL * (it + 1)
				)
			}
			
			renderer.renderItem(stack, model)
			GL.popMatrix()
		}
	}
	
	private fun renderShadow(alpha: Float){
		TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR){
			pos(SHADOW_XZ_MIN, SHADOW_Y, SHADOW_XZ_MIN).tex(0.0, 0.0).color(1F, 1F, 1F, alpha).endVertex()
			pos(SHADOW_XZ_MIN, SHADOW_Y, SHADOW_XZ_MAX).tex(0.0, 1.0).color(1F, 1F, 1F, alpha).endVertex()
			pos(SHADOW_XZ_MAX, SHADOW_Y, SHADOW_XZ_MAX).tex(1.0, 1.0).color(1F, 1F, 1F, alpha).endVertex()
			pos(SHADOW_XZ_MAX, SHADOW_Y, SHADOW_XZ_MIN).tex(1.0, 0.0).color(1F, 1F, 1F, alpha).endVertex()
		}
	}
}
