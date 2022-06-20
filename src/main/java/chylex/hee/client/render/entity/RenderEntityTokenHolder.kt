package chylex.hee.client.render.entity

import chylex.hee.client.model.entity.ModelEntityTokenHolder
import chylex.hee.client.render.util.rotateX
import chylex.hee.client.render.util.rotateY
import chylex.hee.client.render.util.rotateZ
import chylex.hee.client.render.util.scale
import chylex.hee.client.render.util.translateY
import chylex.hee.game.Resource
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.item.ItemPortalToken.TokenType.RARE
import chylex.hee.game.item.ItemPortalToken.TokenType.SOLITARY
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.ResourceLocation
import kotlin.math.pow

@Sided(Side.CLIENT)
class RenderEntityTokenHolder(manager: EntityRendererManager) : EntityRenderer<EntityTokenHolder>(manager) {
	private val textures = mapOf(
		NORMAL   to Resource.Custom("textures/entity/token_holder.png"),
		RARE     to Resource.Custom("textures/entity/token_holder_rare.png"),
		SOLITARY to Resource.Custom("textures/entity/token_holder_solitary.png")
	)
	
	init {
		shadowSize = 0.4F
		shadowOpaque = 0.6F
	}
	
	override fun render(entity: EntityTokenHolder, yaw: Float, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int) {
		val charge = entity.renderCharge.get(partialTicks)
		val scale = 0.25F + (0.25F * charge.pow(1.5F))
		val alpha = 0.35F + (0.475F * charge.pow(5.5F))
		
		matrix.push()
		matrix.translateY(entity.height * 0.5)
		
		matrix.push()
		matrix.scale(scale)
		matrix.rotateY(entity.renderRotation.get(partialTicks))
		matrix.rotateX(55F)
		matrix.rotateZ(55F)
		
		ModelEntityTokenHolder.render(matrix, buffer.getBuffer(RenderType.getEntityTranslucent(getEntityTexture(entity))), combinedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, alpha)
		
		matrix.pop()
		matrix.pop()
	}
	
	override fun getEntityTexture(entity: EntityTokenHolder): ResourceLocation {
		return textures.getValue(entity.tokenType)
	}
}
