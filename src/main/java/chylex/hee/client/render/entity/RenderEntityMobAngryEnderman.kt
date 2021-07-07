package chylex.hee.client.render.entity

import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.renderer.entity.EntityRendererManager

@Sided(Side.CLIENT)
class RenderEntityMobAngryEnderman(manager: EntityRendererManager) : RenderEntityMobAbstractEnderman(manager) {
	override fun getCloneCount(entity: EntityMobAbstractEnderman) = when {
		entity.hurtTime != 0 -> 0
		entity.isAggro       -> 2
		else                 -> 1
	}
}
