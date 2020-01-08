package chylex.hee.client.render.entity
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.RenderEntityItem
import chylex.hee.system.migration.vanilla.RenderManager

@Sided(Side.CLIENT)
class RenderEntityItemNoBob(manager: RenderManager) : RenderEntityItem(manager, MC.itemRenderer){
	override fun shouldBob() = false
}
