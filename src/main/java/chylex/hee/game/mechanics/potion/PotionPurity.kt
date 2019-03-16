package chylex.hee.game.mechanics.potion
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.floorToInt
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.MobEffects.POISON
import net.minecraft.init.MobEffects.WITHER
import net.minecraft.potion.PotionEffect
import kotlin.math.max

object PotionPurity : PotionBase(color = RGB(34, 204, 252), isNegative = false){
	private const val MIN_DURATION = 60 // allow animations to finish, must be > 10
	
	val PURITY = this
	
	override val iconX = 18
	override val iconY = 0
	
	override fun isReady(duration: Int, amplifier: Int): Boolean{
		return true
	}
	
	override fun performEffect(entity: EntityLivingBase, amplifier: Int){
		val purityLevel = amplifier + 1
		
		for((type, effect) in entity.activePotionMap){
			if (type.isBadEffect && effect.duration in MIN_DURATION..INFINITE_DURATION_THRESHOLD){ // TODO handle eternal torment
				when(type){ // UPDATE make sure all potion types with special needs are handled, and the frequencies have not changed
					POISON -> purifySpecial(effect, 25, purityLevel)
					WITHER -> purifySpecial(effect, 40, purityLevel)
					else   -> purifyGeneral(effect, purityLevel, entity.getActivePotionEffect(this)!!.duration)
				}
			}
		}
	}
	
	private fun purifyGeneral(effect: PotionEffect, purityLevel: Int, purityDuration: Int){
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
	
	private fun purifySpecial(effect: PotionEffect, baseFrequency: Int, purityLevel: Int){
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
