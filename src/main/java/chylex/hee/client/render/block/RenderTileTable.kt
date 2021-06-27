package chylex.hee.client.render.block

import chylex.hee.client.MC
import chylex.hee.client.model.ModelHelper
import chylex.hee.client.model.getQuads
import chylex.hee.client.render.gl.rotateY
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.world.center
import chylex.hee.game.world.getTile
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GUI
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack
import net.minecraftforge.client.ForgeHooksClient

@Sided(Side.CLIENT)
class RenderTileTable(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<TileEntityBaseTable>(dispatcher) {
	private companion object {
		private const val COLOR_SHADE = 80F / 255F
		private const val COLOR_ALPHA = 30F / 255F
		private val LIGHT = LightTexture.packLight(15, 0)
		
		private const val Y_OFFSET = 0.8
	}
	
	override fun render(tile: TileEntityBaseTable, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int) {
		val world = tile.world ?: return
		val dustType = tile.tableDustType ?: return
		
		if (tile.pos.up().getTile<TileEntityJarODust>(world)?.layers?.getDustType(DustLayers.Side.BOTTOM) == dustType) {
			return
		}
		
		val rotation = (MC.systemTime % 360000L) / 25F
		val center = tile.pos.center.addY(Y_OFFSET)
		val flip = if (center.subtract(MC.player!!.lookPosVec).dotProduct(Vec3.fromYaw(360F - rotation)) > 0.0) 180F else 0F
		
		matrix.push()
		matrix.translate(0.5, 0.5 + Y_OFFSET, 0.5)
		matrix.rotateY(rotation + flip)
		matrix.scale(0.5F, 0.5F, 0.02F)
		matrix.translate(-0.5, -0.5, -0.5)
		
		val itemStack = ItemStack(dustType.item)
		val itemModel = ForgeHooksClient.handleCameraTransforms(matrix, ModelHelper.getItemModel(itemStack), GUI, false)
		
		val mat = matrix.last
		val builder = ItemRenderer.getBuffer(buffer, RenderTypeLookup.func_239219_a_(itemStack, true), true /* isItem */, false /* hasGlint */)
		
		for(quad in itemModel.getQuads()) {
			builder.addVertexData(mat, quad, COLOR_SHADE, COLOR_SHADE, COLOR_SHADE, COLOR_ALPHA, LIGHT, OverlayTexture.NO_OVERLAY)
		}
		
		matrix.pop()
	}
}
