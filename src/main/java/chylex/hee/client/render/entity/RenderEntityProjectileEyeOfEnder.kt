package chylex.hee.client.render.entity
import chylex.hee.client.MC
import chylex.hee.client.model.ModelHelper
import chylex.hee.client.render.gl.rotateY
import chylex.hee.client.render.gl.translateY
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.Items
import chylex.hee.system.migration.Render
import chylex.hee.system.migration.RenderManager
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityProjectileEyeOfEnder(manager: RenderManager) : Render<EntityProjectileEyeOfEnder>(manager){
	private val renderedItem = ItemStack(Items.ENDER_EYE)
	
	override fun render(entity: EntityProjectileEyeOfEnder, yaw: Float, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int){
		matrix.push()
		matrix.translateY(entity.renderBob.get(partialTicks))
		matrix.rotateY(yaw)
		
		MC.itemRenderer.renderItem(renderedItem, GROUND, false, matrix, buffer, combinedLight, OverlayTexture.NO_OVERLAY, ModelHelper.getItemModel(renderedItem))
		
		matrix.pop()
		
		super.render(entity, yaw, partialTicks, matrix, buffer, combinedLight)
	}
	
	override fun getEntityTexture(entity: EntityProjectileEyeOfEnder): ResourceLocation{
		return PlayerContainer.LOCATION_BLOCKS_TEXTURE
	}
}
