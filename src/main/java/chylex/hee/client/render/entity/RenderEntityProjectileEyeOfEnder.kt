package chylex.hee.client.render.entity
import chylex.hee.client.model.ModelHelper
import chylex.hee.client.render.util.rotateY
import chylex.hee.client.render.util.translateY
import chylex.hee.client.util.MC
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.migration.vanilla.Render
import chylex.hee.system.migration.vanilla.RenderManager
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
