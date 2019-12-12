package chylex.hee.client.render.entity
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.ItemRenderHelper
import chylex.hee.client.util.MC
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.Items
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.ForgeHooksClient

@Sided(Side.CLIENT)
class RenderEntityProjectileEyeOfEnder(manager: RenderManager) : Render<EntityProjectileEyeOfEnder>(manager){
	private companion object{
		private val RENDERED_ITEM = ItemStack(Items.ENDER_EYE)
	}
	
	override fun doRender(entity: EntityProjectileEyeOfEnder, x: Double, y: Double, z: Double, rotationYaw: Float, partialTicks: Float){
		ItemRenderHelper.beginItemModel()
		GL.pushMatrix()
		
		GL.translate(x, y + entity.renderBob.get(partialTicks), z)
		GL.rotate(rotationYaw, 0F, 1F, 0F)
		GL.color(1F, 1F, 1F, 1F)
		
		if (renderOutlines){
			GL.enableColorMaterial()
			GL.enableOutlineMode(getTeamColor(entity))
		}
		
		MC.itemRenderer.renderItem(RENDERED_ITEM, ForgeHooksClient.handleCameraTransforms(ItemRenderHelper.getItemModel(RENDERED_ITEM), GROUND, false))
		
		if (renderOutlines){
			GL.disableOutlineMode()
			GL.disableColorMaterial()
		}
		
		GL.popMatrix()
		ItemRenderHelper.endItemModel()
		
		super.doRender(entity, x, y, z, rotationYaw, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityProjectileEyeOfEnder): ResourceLocation{
		return ItemRenderHelper.TEX_BLOCKS_ITEMS
	}
}
