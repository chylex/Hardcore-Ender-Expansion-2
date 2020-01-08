package chylex.hee.game.mechanics.potion
import chylex.hee.system.migration.MagicValues
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.floorToInt
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.EffectType.BENEFICIAL
import net.minecraft.potion.EffectType.HARMFUL
import kotlin.math.max

object PotionPurity : PotionBase(color = RGB(73, 217, 255), kind = BENEFICIAL){
	const val MIN_DURATION = 60 // allow animations to finish, must be > 10
	
	val TYPE = this.makeType
	
	override val iconX = 18
	override val iconY = 0
	
	override fun isReady(duration: Int, amplifier: Int): Boolean{
		return true
	}
	
	override fun performEffect(entity: EntityLivingBase, amplifier: Int){
		val purityLevel = amplifier + 1
		
		for((type, effect) in entity.activePotionMap){
			if (type.effectType == HARMFUL && effect.duration in MIN_DURATION..INFINITE_DURATION_THRESHOLD){ // TODO handle eternal torment, maybe bad omen?
				when(type){
					Potions.POISON -> purifySpecial(effect, MagicValues.POTION_POISON_TRIGGER_RATE, purityLevel)
					Potions.WITHER -> purifySpecial(effect, MagicValues.POTION_WITHER_TRIGGER_RATE, purityLevel)
					else           -> purifyGeneral(effect, purityLevel, entity.getActivePotionEffect(this)!!.duration)
				}
			}
		}
	}
	
	private fun purifyGeneral(effect: EffectInstance, purityLevel: Int, purityDuration: Int){
		val frequency = when(purityLevel){
			1    -> 10
			2    ->  4
			3    ->  2
			else ->  1
		}
		
		if (purityDuration % frequency == 0){
			effect.duration -= 10 // guarded by MIN_DURATION
		}
	}
	
	private fun purifySpecial(effect: EffectInstance, baseFrequency: Int, purityLevel: Int){
		val realFrequency = (baseFrequency shr effect.amplifier).coerceAtLeast(1)
		
		if (effect.duration % realFrequency == 0){
			val multiplier = when(purityLevel){
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
