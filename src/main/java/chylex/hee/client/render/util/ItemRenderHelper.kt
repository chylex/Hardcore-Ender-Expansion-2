package chylex.hee.client.render.util
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.ITextureObject
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.GL_GREATER

@Sided(Side.CLIENT)
object ItemRenderHelper{
	val TEX_BLOCKS_ITEMS: ResourceLocation = AtlasTexture.LOCATION_BLOCKS_TEXTURE
	private lateinit var texBlocksItemsObj: ITextureObject
	
	fun beginItemModel(){
		with(MC.textureManager){
			texBlocksItemsObj = getTexture(TEX_BLOCKS_ITEMS).also { it.setBlurMipmap(false, false) }
			bindTexture(TEX_BLOCKS_ITEMS)
		}
		
		RenderHelper.enableStandardItemLighting()
		GL.alphaFunc(GL_GREATER, 0.1F)
		GL.enableRescaleNormal()
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
	}
	
	fun getItemModel(stack: ItemStack): IBakedModel{
		return MC.itemRenderer.getItemModelWithOverrides(stack, MC.world, null)
	}
	
	fun endItemModel(){
		GL.disableBlend()
		GL.disableRescaleNormal()
		
		texBlocksItemsObj.restoreLastBlurMipmap()
	}
}
