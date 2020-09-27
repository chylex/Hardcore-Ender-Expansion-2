package chylex.hee.game.entity
import chylex.hee.system.migration.EntityCreature
import chylex.hee.system.migration.EntityLivingBase
import net.minecraft.entity.CreatureAttribute

object CustomCreatureType{
	val ENDER  = CreatureAttribute()
	val DEMON  = CreatureAttribute()
	val SHADOW = CreatureAttribute()
	
	fun isEnder(entity: EntityLivingBase): Boolean{
		return entity is EntityCreature && entity.creatureAttribute == ENDER // TODO more stuff
	}
	
	fun isDemon(entity: EntityLivingBase): Boolean{
		return entity is EntityCreature && entity.creatureAttribute == DEMON
	}
	
	fun isShadow(entity: EntityLivingBase): Boolean{
		return entity is EntityCreature && entity.creatureAttribute == SHADOW
	}
}
