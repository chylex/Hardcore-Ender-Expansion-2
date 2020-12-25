package chylex.hee.client.render.entity

import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityBat
import net.minecraft.client.renderer.entity.BatRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobVampireBat(manager: EntityRendererManager) : BatRenderer(manager) {
	private val texture = Resource.Custom("textures/entity/vampire_bat.png")
	
	override fun getEntityTexture(entity: EntityBat): ResourceLocation {
		return texture
	}
}
