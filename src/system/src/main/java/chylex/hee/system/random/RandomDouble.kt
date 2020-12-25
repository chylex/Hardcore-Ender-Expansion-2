package chylex.hee.system.random

import chylex.hee.system.math.remapRange
import java.util.Random
import kotlin.math.pow

abstract class RandomDouble private constructor(val min: Double, val max: Double) : (Random) -> Double {
	init {
		require(min <= max) { "min must be smaller than or equal to max" }
	}
	
	abstract override fun invoke(rand: Random): Double
	
	// Types
	
	@Suppress("FunctionName")
	companion object {
		fun Constant(value: Double) = object : RandomDouble(value, value) {
			override fun invoke(rand: Random): Double {
				return value
			}
		}
		
		fun Linear(min: Double, max: Double) = object : RandomDouble(min, max) {
			override fun invoke(rand: Random): Double {
				return min + (rand.nextDouble() * (max - min))
			}
		}
		
		fun Exp(min: Double, max: Double, exp: Double) = object : RandomDouble(min, max) {
			override fun invoke(rand: Random): Double {
				return remapRange(rand.nextDouble().pow(exp), (0.0)..(1.0), min..max)
			}
		}
	}
}
