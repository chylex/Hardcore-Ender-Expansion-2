package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.game.mechanics.potion.PotionBase
import chylex.hee.game.mechanics.potion.PotionCorruption
import chylex.hee.game.mechanics.potion.PotionPurity
import chylex.hee.system.migration.vanilla.Potion
import chylex.hee.system.migration.vanilla.PotionType
import chylex.hee.system.migration.vanilla.PotionTypes
import chylex.hee.system.migration.vanilla.Potions

object PotionTypeMap{
	private val TYPE_MAPPING = mapOf(
		Potions.NIGHT_VISION    to PotionTypes.NIGHT_VISION,
		Potions.INVISIBILITY    to PotionTypes.INVISIBILITY,
		Potions.JUMP_BOOST      to PotionTypes.LEAPING,
		Potions.FIRE_RESISTANCE to PotionTypes.FIRE_RESISTANCE,
		Potions.SPEED           to PotionTypes.SWIFTNESS,
		Potions.SLOWNESS        to PotionTypes.SLOWNESS,
		Potions.WATER_BREATHING to PotionTypes.WATER_BREATHING,
		Potions.INSTANT_HEALTH  to PotionTypes.HEALING,
		Potions.INSTANT_DAMAGE  to PotionTypes.HARMING,
		Potions.POISON          to PotionTypes.POISON,
		Potions.REGENERATION    to PotionTypes.REGENERATION,
		Potions.STRENGTH        to PotionTypes.STRENGTH,
		Potions.WEAKNESS        to PotionTypes.WEAKNESS,
		PotionPurity            to makeType(PotionPurity),
		PotionCorruption        to makeType(PotionCorruption)
	)
	
	private val TYPE_NO_EFFECT_OVERRIDES = mutableMapOf<PotionType, PotionType>()
	
	val ALTERED_TYPES
		get() = TYPE_MAPPING.values
	
	private fun makeType(potion: PotionBase): PotionType{
		return PotionType(PotionBrewing.INFO.getValue(potion).baseEffect)
	}
	
	fun getType(potion: Potion): PotionType{
		return TYPE_MAPPING.getValue(potion)
	}
	
	fun getTypeOrWater(potion: Potion): PotionType{
		return TYPE_MAPPING.getOrDefault(potion, PotionTypes.WATER)
	}
	
	fun registerNoEffectOverride(original: PotionType, override: PotionType){
		TYPE_NO_EFFECT_OVERRIDES[original] = override
	}
	
	fun findNoEffectOverride(type: PotionType): PotionType{
		return TYPE_NO_EFFECT_OVERRIDES[type] ?: type
	}
}
