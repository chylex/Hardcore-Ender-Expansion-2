package chylex.hee.client.render.entity
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.entity.RenderEntityItem
import net.minecraft.client.renderer.entity.RenderManager

@Sided(Side.CLIENT)
class RenderEntityItemNoBob(manager: RenderManager) : RenderEntityItem(manager, MC.itemRenderer){
	override fun shouldBob() = false
}
