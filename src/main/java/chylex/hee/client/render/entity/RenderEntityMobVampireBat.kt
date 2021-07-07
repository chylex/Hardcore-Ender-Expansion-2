package chylex.hee.client.render.entity

import chylex.hee.game.Resource
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.renderer.entity.BatRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.entity.passive.BatEntity
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobVampireBat(manager: EntityRendererManager) : BatRenderer(manager) {
	private val texture = Resource.Custom("textures/entity/vampire_bat.png")
	
	override fun getEntityTexture(entity: BatEntity): ResourceLocation {
		return texture
	}
}
