package chylex.hee.client.render.entity.layer
import chylex.hee.client.render.entity.RenderEntityMobSpiderling
import chylex.hee.client.render.util.scale
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.layers.AbstractEyesLayer
import net.minecraft.client.renderer.entity.model.SpiderModel
import net.minecraft.client.renderer.model.ModelRenderer
import net.minecraft.client.renderer.texture.OverlayTexture

@Sided(Side.CLIENT)
class LayerSpiderlingEyes(spiderlingRenderer: RenderEntityMobSpiderling, private val headRenderer: ModelRenderer) : AbstractEyesLayer<EntityMobSpiderling, SpiderModel<EntityMobSpiderling>>(spiderlingRenderer){
	private val renderType = RenderType.getEyes(Resource.Custom("textures/entity/spiderling_eyes.png"))
	
	override fun render(matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, entity: EntityMobSpiderling, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, age: Float, headYaw: Float, headPitch: Float){
		if (entity.isSleeping){
			return
		}
		
		val builder = buffer.getBuffer(getRenderType())
		
		if (headPitch == 0F){
			matrix.push()
			matrix.scale(1.001F) // hack around z-fighting
			headRenderer.render(matrix, builder, 15728640, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F)
			matrix.pop()
		}
		else{
			headRenderer.render(matrix, builder, 15728640, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F)
		}
	}
	
	override fun getRenderType(): RenderType{
		return renderType
	}
}
