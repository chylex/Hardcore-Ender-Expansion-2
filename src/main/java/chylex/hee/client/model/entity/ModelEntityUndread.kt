package chylex.hee.client.model.entity
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.entity.model.AbstractZombieModel

@Sided(Side.CLIENT)
class ModelEntityUndread private constructor(modelSize: Float, textureWidth: Int, textureHeight: Int) : AbstractZombieModel<EntityMobUndread>(modelSize, 0F, textureWidth, textureHeight){
	constructor(modelSize: Float, tallTexture: Boolean) : this(modelSize, 64, if (tallTexture) 32 else 64)
	constructor() : this(0F, false)
	
	override fun func_212850_a_(entity: EntityMobUndread): Boolean{
		return entity.isAggressive
	}
}
