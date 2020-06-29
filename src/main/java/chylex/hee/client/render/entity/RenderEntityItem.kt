package chylex.hee.client.render.entity
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.RenderManager
import net.minecraft.client.renderer.entity.ItemRenderer

@Sided(Side.CLIENT)
class RenderEntityItem(manager: RenderManager) : ItemRenderer(manager, MC.itemRenderer)
