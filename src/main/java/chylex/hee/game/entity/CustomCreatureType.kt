package chylex.hee.game.entity
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.common.util.EnumHelper

object CustomCreatureType{
	private val ENDER  = EnumHelper.addCreatureAttribute("HEE_ENDER")
	private val DEMON  = EnumHelper.addCreatureAttribute("HEE_DEMON")
	private val SHADOW = EnumHelper.addCreatureAttribute("HEE_SHADOW")
	
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
