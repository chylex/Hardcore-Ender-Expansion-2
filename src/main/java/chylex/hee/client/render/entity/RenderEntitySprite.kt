package chylex.hee.client.render.entity
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.RenderManager
import net.minecraft.client.renderer.entity.SpriteRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.IRendersAsItem

@Sided(Side.CLIENT)
class RenderEntitySprite<T>(manager: RenderManager) : SpriteRenderer<T>(manager, MC.itemRenderer) where T : Entity, T : IRendersAsItem
