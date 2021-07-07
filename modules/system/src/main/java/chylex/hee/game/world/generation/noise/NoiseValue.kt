package chylex.hee.game.world.generation.noise

import chylex.hee.util.math.remapRange
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

class NoiseValue(var value: Double) {
	inline fun then(process: (Double) -> Double) {
		value = process(value)
	}
	
	fun multiply(mp: Double) = then {
		it * mp
	}
	
	fun redistribute(power: Double) = then {
		it.pow(power)
	}
	
	fun terrace(steps: Int) = then {
		(it * steps).roundToInt().toDouble() / steps
	}
	
	fun coerce(minimum: Double = 0.0, maximum: Double = 1.0) = then {
		it.coerceIn(minimum, maximum)
	}
	
	fun remap(oldRange: ClosedFloatingPointRange<Double>, newRange: ClosedFloatingPointRange<Double>) = then {
		remapRange(it, oldRange, newRange)
	}
	
	fun remap(newRange: ClosedFloatingPointRange<Double>) = then {
		remapRange(it, (0.0)..(1.0), newRange)
	}
	
	inline fun ifNonZero(block: NoiseValue.() -> Unit) {
		if (abs(value) > 0.001) {
			block()
		}
	}
}
