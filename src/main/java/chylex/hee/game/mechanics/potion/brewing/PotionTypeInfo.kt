package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.nbt
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.potion.PotionUtils

class PotionTypeInfo(
	val potion: Potion,
	private val duration: Duration? = null,
	private val maxLevel: Int
){
	fun getBasePotion(item: Item): ItemStack{
		return PotionUtils.appendEffects(PotionItems.getBottle(item, potion), listOf(PotionEffect(potion, duration?.baseTicks ?: 0, 0)))
	}
	
	class Duration(val baseTicks: Int, val stepTicks: Int, val maxSteps: Int){
		fun calculate(steps: Int, amplifier: Int): Int{
			var ticks = baseTicks + (stepTicks * steps)
			
			repeat(amplifier){
				val mp = if (it == 0)
					0.4
				else
					0.5
				
				ticks = (ticks * mp).floorToInt()
			}
			
			return ticks
		}
	}
	
	inner class Instance(private val stack: ItemStack, private val effect: PotionEffect){
		private val durationSteps
			get() = stack.heeTagOrNull?.getIntegerOrNull("DurationSteps") ?: 0
		
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
				
				val baseItem = info.getBasePotion(stack.item)
				val baseEffect = PotionEffect(reversed, 0, 0, effect.isAmbient, effect.doesShowParticles())
				
				return info.Instance(baseItem, baseEffect).createNewEffect(durationSteps, baseEffect.amplifier)
			}
		
		private fun createNewEffect(durationSteps: Int, amplifier: Int): ItemStack{
			val newDuration = duration?.calculate(durationSteps, amplifier) ?: 0
			val newEffect = PotionEffect(potion, newDuration, amplifier, effect.isAmbient, effect.doesShowParticles())
			val newStack = stack.copy()
			
			newStack.heeTag.setInteger("DurationSteps", durationSteps)
			newStack.nbt.removeTag(PotionItems.CUSTOM_EFFECTS_TAG)
			
			return PotionUtils.appendEffects(newStack, listOf(newEffect))
		}
	}
}
