package chylex.hee.client.render.entity
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
import net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.GL_GREATER

@SideOnly(Side.CLIENT)
class RenderEntityProjectileEyeOfEnder(manager: RenderManager) : Render<EntityProjectileEyeOfEnder>(manager){
	private companion object{
		private val TEX_BLOCKS_ITEMS = TextureMap.LOCATION_BLOCKS_TEXTURE
		private val RENDERED_ITEM = ItemStack(Items.ENDER_EYE)
	}
	
	override fun doRender(entity: EntityProjectileEyeOfEnder, x: Double, y: Double, z: Double, rotationYaw: Float, partialTicks: Float){
		val textureManager = MC.textureManager
		val itemRenderer = MC.itemRenderer
		
		val texObj = textureManager.getTexture(getEntityTexture(entity)).also { it.setBlurMipmap(false, false) }
		textureManager.bindTexture(TEX_BLOCKS_ITEMS)
		
		GL.pushMatrix()
		GL.translate(x, y + entity.renderBob.get(partialTicks), z)
		GL.rotate(rotationYaw, 0F, 1F, 0F)
		
		RenderHelper.enableStandardItemLighting()
		GL.alphaFunc(GL_GREATER, 0.1F)
		GL.enableRescaleNormal()
		GL.enableBlend()
		GL.tryBlendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)
		
		GL.color(1F, 1F, 1F, 1F)
		
		if (renderOutlines){
			GL.enableColorMaterial()
			GL.enableOutlineMode(getTeamColor(entity))
		}
		
		val baseModel = itemRenderer.getItemModelWithOverrides(RENDERED_ITEM, entity.world, null)
		itemRenderer.renderItem(RENDERED_ITEM, ForgeHooksClient.handleCameraTransforms(baseModel, TransformType.GROUND, false))
		
		if (renderOutlines){
			GL.disableOutlineMode()
			GL.disableColorMaterial()
		}
		
		GL.disableBlend()
		GL.disableRescaleNormal()
		
		GL.popMatrix()
		texObj.restoreLastBlurMipmap()
		
		super.doRender(entity, x, y, z, rotationYaw, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityProjectileEyeOfEnder): ResourceLocation{
		return TEX_BLOCKS_ITEMS
	}
}
