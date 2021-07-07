package chylex.hee.game.entity

import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.LivingEntity

object CustomCreatureType {
	val ENDER = CreatureAttribute()
	val DEMON = CreatureAttribute()
	val SHADOW = CreatureAttribute()
	
	fun isEnder(entity: LivingEntity): Boolean {
		return entity is CreatureEntity && entity.creatureAttribute == ENDER // TODO more stuff
	}
	
	fun isDemon(entity: LivingEntity): Boolean {
		return entity is CreatureEntity && entity.creatureAttribute == DEMON
	}
	
	fun isShadow(entity: LivingEntity): Boolean {
		return entity is CreatureEntity && entity.creatureAttribute == SHADOW
	}
}
