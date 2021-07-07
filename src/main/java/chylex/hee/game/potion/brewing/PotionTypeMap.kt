package chylex.hee.game.potion.brewing

import chylex.hee.game.potion.BanishmentEffect
import chylex.hee.game.potion.CorruptionEffect
import chylex.hee.game.potion.PurityEffect
import net.minecraft.potion.Effect
import net.minecraft.potion.Effects
import net.minecraft.potion.Potion
import net.minecraft.potion.Potions

object PotionTypeMap {
	private val TYPE_MAPPING = mapOf(
		Effects.NIGHT_VISION    to Potions.NIGHT_VISION,
		Effects.INVISIBILITY    to Potions.INVISIBILITY,
		Effects.JUMP_BOOST      to Potions.LEAPING,
		Effects.FIRE_RESISTANCE to Potions.FIRE_RESISTANCE,
		Effects.SPEED           to Potions.SWIFTNESS,
		Effects.SLOWNESS        to Potions.SLOWNESS,
		Effects.WATER_BREATHING to Potions.WATER_BREATHING,
		Effects.INSTANT_HEALTH  to Potions.HEALING,
		Effects.INSTANT_DAMAGE  to Potions.HARMING,
		Effects.POISON          to Potions.POISON,
		Effects.REGENERATION    to Potions.REGENERATION,
		Effects.STRENGTH        to Potions.STRENGTH,
		Effects.WEAKNESS        to Potions.WEAKNESS,
		PurityEffect            to makePotion(PurityEffect),
		CorruptionEffect        to makePotion(CorruptionEffect),
		BanishmentEffect        to makePotion(BanishmentEffect)
	)
	
	private val TYPE_NO_EFFECT_OVERRIDES = mutableMapOf<Potion, Potion>()
	
	val ALTERED_TYPES
		get() = TYPE_MAPPING.values
	
	private fun makePotion(effect: Effect): Potion {
		return Potion(PotionBrewing.INFO.getValue(effect).baseEffect)
	}
	
	fun getPotion(effect: Effect): Potion {
		return TYPE_MAPPING.getValue(effect)
	}
	
	fun getPotionOrWater(effect: Effect): Potion {
		return TYPE_MAPPING.getOrDefault(effect, Potions.WATER)
	}
	
	fun registerNoEffectOverride(original: Potion, override: Potion) {
		TYPE_NO_EFFECT_OVERRIDES[original] = override
	}
	
	fun findNoEffectOverride(potion: Potion): Potion {
		return TYPE_NO_EFFECT_OVERRIDES[potion] ?: potion
	}
}
