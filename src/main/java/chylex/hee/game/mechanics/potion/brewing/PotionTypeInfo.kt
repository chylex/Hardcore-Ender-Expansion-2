package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nbt
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.potion.PotionUtils
import kotlin.math.abs

class PotionTypeInfo(
	val potion: Potion,
	private val duration: Duration? = null,
	private val maxLevel: Int
){
	val baseEffect
		get() = PotionEffect(potion, duration?.baseTicks ?: 0, 0)
	
	val vanillaOverrideStrongEffect
		get() = PotionEffect(potion, duration?.getDuration(0, 1) ?: 0, 1)
	
	val vanillaOverrideLongEffect
		get() = PotionEffect(potion, duration?.getDuration(1, 0) ?: 0, 0)
	
	class Duration(val baseTicks: Int, val stepTicks: Int, val maxSteps: Int){
		private fun getBaseDuration(steps: Int): Int{
			return baseTicks + (stepTicks * steps)
		}
		
		private fun mp(amplifier: Int): Double{
			return if (amplifier == 0)
				0.4
			else
				0.5
		}
		
		fun getDuration(steps: Int, amplifier: Int): Int{
			var ticks = getBaseDuration(steps)
			
			repeat(amplifier){
				ticks = (ticks * mp(it)).floorToInt()
			}
			
			return ticks
		}
		
		fun getSteps(duration: Int, amplifier: Int): Int{
			var unrolled = duration.toDouble()
			
			repeat(amplifier){
				unrolled /= mp(it)
			}
			
			return (0..maxSteps).minBy { abs(getBaseDuration(it) - unrolled) } ?: 0
		}
	}
	
	inner class Instance(private val stack: ItemStack, private val effect: PotionEffect){
		private val durationSteps = duration?.getSteps(effect.duration, amplifier) ?: 0
		
		private val amplifier
			get() = effect.amplifier
		
		val canIncreaseLevel
			get() = (amplifier + 1) < maxLevel
		
		val canIncreaseDuration
			get() = duration?.let { durationSteps < it.maxSteps }
		
		val canReverse
			get() = PotionBrewing.REVERSAL.containsKey(potion)
		
		val withIncreasedLevel
			get() = createNewEffect(durationSteps, amplifier + 1)
		
		val withIncreasedDuration
			get() = createNewEffect(durationSteps + 1, amplifier)
		
		val afterReversal: ItemStack?
			get(){
				val reversed = PotionBrewing.REVERSAL[potion] ?: return null
				val info = PotionBrewing.INFO[reversed] ?: return null
				
				if (durationSteps == 0 && amplifier == 0){
					return PotionItems.getBottle(stack.item, potion, withBaseEffect = true)
				}
				
				val newItem = PotionItems.getBottle(stack.item, potion, withBaseEffect = false)
				val newEffect = PotionEffect(reversed, 0, 0, effect.isAmbient, effect.doesShowParticles())
				
				return info.Instance(newItem, newEffect).createNewEffect(durationSteps, newEffect.amplifier)
			}
		
		private fun createNewEffect(durationSteps: Int, amplifier: Int): ItemStack{
			val newDuration = duration?.getDuration(durationSteps, amplifier) ?: 0
			val newEffect = PotionEffect(potion, newDuration, amplifier, effect.isAmbient, effect.doesShowParticles())
			
			return with(stack.copy()){
				nbt.removeTag(PotionItems.CUSTOM_EFFECTS_TAG)
				
				PotionUtils.addPotionToItemStack(this, PotionItems.findNoEffectOverride(PotionUtils.getPotionFromItem(this)))
				PotionUtils.appendEffects(this, listOf(newEffect))
			}
		}
	}
}
