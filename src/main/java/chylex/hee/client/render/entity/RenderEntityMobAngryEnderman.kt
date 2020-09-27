package chylex.hee.client.render.entity
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.RenderManager

@Sided(Side.CLIENT)
class RenderEntityMobAngryEnderman(manager: RenderManager) : RenderEntityMobAbstractEnderman(manager){
	override fun getCloneCount(entity: EntityMobAbstractEnderman) = when{
		entity.hurtTime != 0 -> 0
		entity.isAggro       -> 2
		else                 -> 1
	}
}
