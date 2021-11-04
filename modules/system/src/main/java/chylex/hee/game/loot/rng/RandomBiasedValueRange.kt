package chylex.hee.game.loot.rng

import chylex.hee.game.Resource
import chylex.hee.util.random.nextBiasedFloat
import net.minecraft.loot.IRandomRange
import net.minecraft.util.ResourceLocation
import java.util.Random
import kotlin.math.roundToInt

class RandomBiasedValueRange(private val min: Float, private val max: Float, private val highestChanceValue: Float, private val biasSoftener: Float) : IRandomRange {
	companion object {
		val LOCATION = Resource.Custom("biased_range")
	}
	
	init {
		require(highestChanceValue in min..max) { "highestChanceValue must be between min and max" }
	}
	
	override fun generateInt(rand: Random): Int {
		return (highestChanceValue + (rand.nextBiasedFloat(biasSoftener) * (0.5F + max - highestChanceValue)) - (rand.nextBiasedFloat(biasSoftener) * (0.5F + highestChanceValue - min))).roundToInt()
	}
	
	override fun getType(): ResourceLocation {
		return LOCATION
	}
}
