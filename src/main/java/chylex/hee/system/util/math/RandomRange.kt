package chylex.hee.system.util.math
import chylex.hee.system.util.remapRange
import java.util.Random
import kotlin.math.pow

sealed class RandomRange(val min: Double, val max: Double){
	init{
		if (min > max){
			throw IllegalArgumentException("min must be smaller than or equal to max")
		}
	}
	
	abstract fun nextDouble(rand: Random): Double
	fun nextFloat(rand: Random) = nextDouble(rand).toFloat()
	
	// Types
	
	class Linear(min: Double, max: Double) : RandomRange(min, max){
		override fun nextDouble(rand: Random): Double{
			return min + (rand.nextDouble() * (max - min))
		}
	}
	
	class Exp(min: Double, max: Double, private val exp: Double) : RandomRange(min, max){
		override fun nextDouble(rand: Random): Double{
			return remapRange(rand.nextDouble().pow(exp), (0.0)..(1.0), min..max)
		}
	}
}
