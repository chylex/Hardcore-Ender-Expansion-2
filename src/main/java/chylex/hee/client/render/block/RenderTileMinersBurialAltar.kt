package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.ItemRenderHelper
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.init.ModItems
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.client.ForgeHooksClient

@Sided(Side.CLIENT)
object RenderTileMinersBurialAltar : TileEntityRenderer<TileEntityMinersBurialAltar>(){
	private val PUZZLE_MEDALLION = ItemStack(ModItems.PUZZLE_MEDALLION)
	private const val SCALE_XZ = 1.85F
	
	override fun render(tile: TileEntityMinersBurialAltar, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int){
		if (!tile.hasMedallion){
			return
		}
		
		ItemRenderHelper.beginItemModel()
		GL.pushMatrix()
		GL.translate(x, y, z)
		GL.pushMatrix()
		
		GL.translate(0.5F, 0.7725F - (0.035F * tile.clientMedallionAnimProgress), 0.625F + 0.125F * (SCALE_XZ - 1F))
		GL.rotate(270F, 1F, 0F, 0F)
		GL.scale(SCALE_XZ, SCALE_XZ, 1.5F)
		
		MC.itemRenderer.renderItem(PUZZLE_MEDALLION, ForgeHooksClient.handleCameraTransforms(ItemRenderHelper.getItemModel(PUZZLE_MEDALLION), GROUND, false))
		
		GL.popMatrix()
		GL.popMatrix()
		ItemRenderHelper.endItemModel()
	}
}
