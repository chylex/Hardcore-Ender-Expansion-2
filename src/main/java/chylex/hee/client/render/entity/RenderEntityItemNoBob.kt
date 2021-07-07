package chylex.hee.client.render.entity

import chylex.hee.client.util.MC
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.ItemRenderer

@Sided(Side.CLIENT)
class RenderEntityItemNoBob(manager: EntityRendererManager) : ItemRenderer(manager, MC.itemRenderer) {
	override fun shouldBob() = false
}
