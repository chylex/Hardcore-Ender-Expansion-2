package chylex.hee.client.render.entity

import chylex.hee.client.MC
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.ItemRenderer

@Sided(Side.CLIENT)
class RenderEntityItem(manager: EntityRendererManager) : ItemRenderer(manager, MC.itemRenderer)
