package chylex.hee.game.potion.brewing

import chylex.hee.game.item.util.nbt
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.util.math.floorToInt
import net.minecraft.item.ItemStack
import net.minecraft.potion.Effect
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.PotionUtils
import kotlin.math.abs

class PotionTypeInfo(
	val effect: Effect,
	private val duration: Duration? = null,
	private val maxLevel: Int,
) {
	val baseEffect
		get() = effect.makeInstance(duration?.baseTicks ?: 0, 0)
	
	val vanillaOverrideStrongEffect
		get() = effect.makeInstance(duration?.getDuration(0, 1) ?: 0, 1)
	
	val vanillaOverrideLongEffect
		get() = effect.makeInstance(duration?.getDuration(1, 0) ?: 0, 0)
	
	class Duration(val baseTicks: Int, val stepTicks: Int, val maxSteps: Int) {
		private fun getBaseDuration(steps: Int): Int {
			return baseTicks + (stepTicks * steps)
		}
		
		private fun mp(amplifier: Int): Double {
			return if (amplifier == 0)
				0.4
			else
				0.5
		}
		
		fun getDuration(steps: Int, amplifier: Int): Int {
			var ticks = getBaseDuration(steps)
			
			repeat(amplifier) {
				ticks = (ticks * mp(it)).floorToInt()
			}
			
			return ticks
		}
		
		fun getSteps(duration: Int, amplifier: Int): Int {
			var unrolled = duration.toDouble()
			
			repeat(amplifier) {
				unrolled /= mp(it)
			}
			
			return (0..maxSteps).minByOrNull { abs(getBaseDuration(it) - unrolled) } ?: 0
		}
	}
	
	inner class Instance(private val stack: ItemStack, private val instance: EffectInstance) {
		private val durationSteps = duration?.getSteps(instance.duration, amplifier) ?: 0
		
		private val amplifier
			get() = instance.amplifier
		
		val canIncreaseLevel
			get() = (amplifier + 1) < maxLevel
		
		val canIncreaseDuration
			get() = duration?.let { durationSteps < it.maxSteps }
		
		val canReverse
			get() = PotionBrewing.REVERSAL.containsKey(effect)
		
		val withIncreasedLevel
			get() = createNewEffect(durationSteps, amplifier + 1)
		
		val withIncreasedDuration
			get() = createNewEffect(durationSteps + 1, amplifier)
		
		val afterReversal: ItemStack?
			get() {
				val reversed = PotionBrewing.REVERSAL[effect] ?: return null
				val info = PotionBrewing.INFO[reversed] ?: return null
				
				if (durationSteps == 0 && amplifier == 0) {
					return PotionItems.getBottle(stack.item, reversed, withBaseEffect = true)
				}
				
				val newItem = PotionItems.getBottle(stack.item, reversed, withBaseEffect = false)
				val newEffect = reversed.makeInstance(0, 0, instance.isAmbient, instance.doesShowParticles())
				
				return info.Instance(newItem, newEffect).createNewEffect(durationSteps, newEffect.amplifier)
			}
		
		private fun createNewEffect(durationSteps: Int, amplifier: Int): ItemStack {
			val newDuration = duration?.getDuration(durationSteps, amplifier) ?: 0
			val newEffect = effect.makeInstance(newDuration, amplifier, instance.isAmbient, instance.doesShowParticles())
			
			return with(stack.copy()) {
				nbt.remove(PotionItems.CUSTOM_EFFECTS_TAG)
				
				PotionUtils.addPotionToItemStack(this, PotionTypeMap.findNoEffectOverride(PotionUtils.getPotionFromItem(this)))
				PotionUtils.appendEffects(this, listOf(newEffect))
			}
		}
	}
}
