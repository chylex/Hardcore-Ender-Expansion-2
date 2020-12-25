package chylex.hee.game.loot.rng

import chylex.hee.system.random.nextBiasedFloat
import net.minecraft.world.storage.loot.RandomValueRange
import java.util.Random
import kotlin.math.roundToInt

class RandomBiasedValueRange(range: ClosedFloatingPointRange<Float>, private val highestChanceValue: Float, private val biasSoftener: Float) : RandomValueRange(range.start, range.endInclusive) {
	init {
		require(highestChanceValue in min..max) { "highestChanceValue must be between min and max" }
	}
	
	/*

import java.util.Random
import chylex.hee.system.random.nextBiasedFloat
import kotlin.math.roundToInt

val rand = Random()

(1..100000).map {
	val min = 3F
	val max = 10F
	val highestChanceValue = 6.5F
	val biasSoftener = 3F
	(highestChanceValue + (rand.nextBiasedFloat(biasSoftener) * (0.5F + max - highestChanceValue)) - (rand.nextBiasedFloat(biasSoftener) * (0.5F + highestChanceValue - min))).roundToInt()
}.groupBy { it }.mapValues { it.value.size }.toList().sortedBy { it.first }
	
	 */
	
	override fun generateInt(rand: Random): Int {
		return (highestChanceValue + (rand.nextBiasedFloat(biasSoftener) * (0.5F + max - highestChanceValue)) - (rand.nextBiasedFloat(biasSoftener) * (0.5F + highestChanceValue - min))).roundToInt()
	}
	
	override fun generateFloat(rand: Random): Float {
		return highestChanceValue + (rand.nextBiasedFloat(biasSoftener) * (max - highestChanceValue)) - (rand.nextBiasedFloat(biasSoftener) * (highestChanceValue - min))
	}
}
