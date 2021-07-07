package chylex.hee.client.render.block

import chylex.hee.client.model.util.ModelHelper
import chylex.hee.client.render.util.rotateX
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.init.ModItems
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack

@Sided(Side.CLIENT)
class RenderTileMinersBurialAltar(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<TileEntityMinersBurialAltar>(dispatcher) {
	private companion object {
		private val PUZZLE_MEDALLION = ItemStack(ModItems.PUZZLE_MEDALLION)
		private const val SCALE_XZ = 1.85F
	}
	
	override fun render(tile: TileEntityMinersBurialAltar, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int) {
		if (!tile.hasMedallion) {
			return
		}
		
		matrix.push()
		matrix.translate(0.5, 0.7725 - (0.035 * tile.clientMedallionAnimProgress), 0.5 + (0.125 * SCALE_XZ))
		matrix.rotateX(270F)
		matrix.scale(SCALE_XZ, SCALE_XZ, 1.5F)
		
		MC.itemRenderer.renderItem(PUZZLE_MEDALLION, GROUND, false, matrix, buffer, combinedLight, OverlayTexture.NO_OVERLAY, ModelHelper.getItemModel(PUZZLE_MEDALLION))
		
		matrix.pop()
	}
}
