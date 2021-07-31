package chylex.hee.game.potion

import chylex.hee.game.MagicValues
import chylex.hee.game.potion.brewing.PotionBrewing.INFINITE_DURATION_THRESHOLD
import chylex.hee.game.potion.brewing.PotionTypeMap
import chylex.hee.util.color.RGB
import chylex.hee.util.math.floorToInt
import net.minecraft.entity.LivingEntity
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.EffectType.BENEFICIAL
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraft.potion.Effects
import kotlin.math.max

object PurityEffect : HeeEffect(BENEFICIAL, RGB(73, 217, 255)) {
	const val MIN_DURATION = 60 // allow animations to finish, must be > 10
	
	val POTION
		get() = PotionTypeMap.getPotion(this)
	
	override fun isReady(duration: Int, amplifier: Int): Boolean {
		return true
	}
	
	override fun performEffect(entity: LivingEntity, amplifier: Int) {
		val purityLevel = amplifier + 1
		
		for ((type, effect) in entity.activePotionMap) {
			if (type.effectType == HARMFUL && effect.duration in MIN_DURATION..INFINITE_DURATION_THRESHOLD) { // TODO handle eternal torment, maybe bad omen?
				when (type) {
					Effects.POISON -> purifySpecial(effect, MagicValues.POTION_POISON_TRIGGER_RATE, purityLevel)
					Effects.WITHER -> purifySpecial(effect, MagicValues.POTION_WITHER_TRIGGER_RATE, purityLevel)
					else           -> purifyGeneral(effect, purityLevel, entity.getActivePotionEffect(this)!!.duration)
				}
			}
		}
	}
	
	private fun purifyGeneral(effect: EffectInstance, purityLevel: Int, purityDuration: Int) {
		val frequency = when (purityLevel) {
			1    -> 10
			2    ->  4
			3    ->  2
			else ->  1
		}
		
		if (purityDuration % frequency == 0) {
			effect.duration -= 10 // guarded by MIN_DURATION
		}
	}
	
	private fun purifySpecial(effect: EffectInstance, baseFrequency: Int, purityLevel: Int) {
		val realFrequency = (baseFrequency shr effect.amplifier).coerceAtLeast(1)
		
		if (effect.duration % realFrequency == 0) {
			val multiplier = when (purityLevel) {
				1    ->  1F
				2    ->  1.42F // does this number make sense...? it almost works out though
				3    ->  5F
				else -> 10F
			}
			
			val decreaseBy = (realFrequency * multiplier).floorToInt()
			effect.duration = max(11, effect.duration - decreaseBy)
		}
	}
}
