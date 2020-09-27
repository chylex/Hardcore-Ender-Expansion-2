package chylex.hee.client.render.entity
import chylex.hee.client.MC
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.RenderManager
import net.minecraft.client.renderer.entity.ItemRenderer

@Sided(Side.CLIENT)
class RenderEntityItemNoBob(manager: RenderManager) : ItemRenderer(manager, MC.itemRenderer){
	override fun shouldBob() = false
}
