package chylex.hee.client.render.entity
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.entity.RenderManager

@Sided(Side.CLIENT)
class RenderEntityMobAngryEnderman(manager: RenderManager) : RenderEntityMobAbstractEnderman(manager){
	override fun getCloneCount(entity: EntityMobAbstractEnderman) = when{
		entity.hurtTime != 0 -> 0
		entity.isAggressive  -> 2
		else                 -> 1
	}
}
