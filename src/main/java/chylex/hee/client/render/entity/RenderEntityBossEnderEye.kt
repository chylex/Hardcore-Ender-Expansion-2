package chylex.hee.client.render.entity

import chylex.hee.client.model.entity.ModelEntityBossEnderEye
import chylex.hee.client.model.entity.ModelEntityBossEnderEye.SCALE
import chylex.hee.client.render.entity.layer.LayerEnderEyeLaser
import chylex.hee.client.render.util.scale
import chylex.hee.game.Resource
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityBossEnderEye(manager: EntityRendererManager) : MobRenderer<EntityBossEnderEye, ModelEntityBossEnderEye>(manager, ModelEntityBossEnderEye, SCALE) {
	private val texture = Resource.Custom("textures/entity/ender_eye.png")
	
	init {
		addLayer(LayerEnderEyeLaser(this))
	}
	
	override fun preRenderCallback(entity: EntityBossEnderEye, matrix: MatrixStack, partialTicks: Float) {
		matrix.scale(SCALE)
		super.preRenderCallback(entity, matrix, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityBossEnderEye): ResourceLocation {
		return texture
	}
}
