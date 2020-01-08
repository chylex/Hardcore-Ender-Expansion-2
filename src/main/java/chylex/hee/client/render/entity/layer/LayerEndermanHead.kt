package chylex.hee.client.render.entity.layer
import chylex.hee.client.render.block.RenderTileEndermanHead
import chylex.hee.init.ModItems
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.entity.player.AbstractClientPlayerEntity
import net.minecraft.client.renderer.entity.PlayerRenderer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.client.renderer.entity.model.PlayerModel
import net.minecraft.inventory.EquipmentSlotType.HEAD

@Sided(Side.CLIENT)
class LayerEndermanHead(renderer: PlayerRenderer) : LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>(renderer){
	override fun render(entity: AbstractClientPlayerEntity, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, entityTickTime: Float, rotationYaw: Float, rotationPitch: Float, scale: Float){
		if (entity.getItemStackFromSlot(HEAD).item === ModItems.ENDERMAN_HEAD){
			RenderTileEndermanHead.AsHeadLayer(entity, entityModel.bipedHead)
		}
	}
	
	override fun shouldCombineTextures() = false
}
