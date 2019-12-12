package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.init.ModItems
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.RenderItem
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.client.ForgeHooksClient
import org.lwjgl.opengl.GL11.GL_GREATER

@Sided(Side.CLIENT)
object RenderTileMinersBurialAltar : TileEntitySpecialRenderer<TileEntityMinersBurialAltar>(){
	private val TEX_BLOCKS_ITEMS = TextureMap.LOCATION_BLOCKS_TEXTURE
	private val PUZZLE_MEDALLION = ItemStack(ModItems.PUZZLE_MEDALLION)
	
	private const val SCALE_XZ = 1.85F
	
	override fun render(tile: TileEntityMinersBurialAltar, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		if (!tile.hasMedallion){
			return
		}
		
		val textureManager = MC.textureManager
		val itemRenderer = MC.itemRenderer
		
		val texObj = textureManager.getTexture(TEX_BLOCKS_ITEMS).also { it.setBlurMipmap(false, false) }
		textureManager.bindTexture(TEX_BLOCKS_ITEMS)
		
		GL.pushMatrix()
		GL.translate(x, y, z)
		
		RenderHelper.enableStandardItemLighting()
		GL.alphaFunc(GL_GREATER, 0.1F)
		GL.enableRescaleNormal()
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
		
		renderItemStack(itemRenderer, tile.clientMedallionAnimProgress)
		
		GL.disableBlend()
		GL.disableRescaleNormal()
		
		GL.popMatrix()
		texObj.restoreLastBlurMipmap()
	}
	
	private fun renderItemStack(renderer: RenderItem, animProgress: Float){
		GL.pushMatrix()
		
		GL.translate(0.5F, 0.7725F - (0.035F * animProgress), 0.625F + 0.125F * (SCALE_XZ - 1F))
		GL.rotate(270F, 1F, 0F, 0F)
		GL.scale(SCALE_XZ, SCALE_XZ, 1.5F)
		
		renderer.renderItem(PUZZLE_MEDALLION, ForgeHooksClient.handleCameraTransforms(renderer.getItemModelWithOverrides(PUZZLE_MEDALLION, world, null), GROUND, false))
		
		GL.popMatrix()
	}
}
