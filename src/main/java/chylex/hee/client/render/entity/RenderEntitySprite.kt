package chylex.hee.client.render.entity
import chylex.hee.client.MC
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.RenderManager
import net.minecraft.client.renderer.entity.SpriteRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.IRendersAsItem

@Sided(Side.CLIENT)
class RenderEntitySprite<T>(manager: RenderManager) : SpriteRenderer<T>(manager, MC.itemRenderer) where T : Entity, T : IRendersAsItem
