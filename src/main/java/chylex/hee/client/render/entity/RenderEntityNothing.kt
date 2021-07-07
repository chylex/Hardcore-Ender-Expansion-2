package chylex.hee.client.render.entity

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.renderer.culling.ClippingHelper
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityNothing(manager: EntityRendererManager) : EntityRenderer<Entity>(manager) {
	override fun shouldRender(entity: Entity, camera: ClippingHelper, camX: Double, camY: Double, camZ: Double) = false
	override fun getEntityTexture(entity: Entity): ResourceLocation? = null
}
