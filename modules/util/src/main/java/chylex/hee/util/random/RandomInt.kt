package chylex.hee.util.random

import chylex.hee.util.math.floorToInt
import java.util.Random

abstract class RandomInt private constructor(val min: Int, val max: Int) : (Random) -> Int {
	init {
		require(min <= max) { "min must be smaller than or equal to max" }
	}
	
	abstract override fun invoke(rand: Random): Int
	
	// Types
	
	@Suppress("FunctionName")
	companion object {
		fun Constant(value: Int) = object : RandomInt(value, value) {
			override fun invoke(rand: Random): Int {
				return value
			}
		}
		
		fun Linear(min: Int, max: Int) = object : RandomInt(min, max) {
			override fun invoke(rand: Random): Int {
				return rand.nextInt(min, max)
			}
		}
		
		fun Biased(min: Int, max: Int, biasSoftener: Float) = object : RandomInt(min, max) {
			override fun invoke(rand: Random): Int {
				return min + ((max - min + 1) * rand.nextBiasedFloat(biasSoftener)).floorToInt()
			}
		}
	}
}
