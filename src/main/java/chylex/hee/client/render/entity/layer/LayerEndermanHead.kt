package chylex.hee.client.render.entity.layer
import chylex.hee.client.render.block.RenderTileEndermanHead
import chylex.hee.init.ModItems
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot.HEAD
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class LayerEndermanHead(private val headModel: ModelRenderer) : LayerRenderer<EntityPlayer>{
	override fun doRenderLayer(entity: EntityPlayer, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, entityTickTime: Float, rotationYaw: Float, rotationPitch: Float, scale: Float){
		if (entity.getItemStackFromSlot(HEAD).item === ModItems.ENDERMAN_HEAD){
			RenderTileEndermanHead.AsHeadLayer(entity, headModel)
		}
	}
	
	override fun shouldCombineTextures() = false
}
