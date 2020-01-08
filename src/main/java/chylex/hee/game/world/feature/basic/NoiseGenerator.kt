package chylex.hee.game.world.feature.basic
import chylex.hee.system.util.remapRange
import net.minecraft.world.gen.PerlinNoiseGenerator
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

sealed class NoiseGenerator(private val xScale: Double, private val zScale: Double){
	class NoiseValue(var value: Double){
		inline fun then(process: (Double) -> Double){
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
		
		fun ifNonZero(block: NoiseValue.() -> Unit){
			if (abs(value) > 0.001){
				block()
			}
		}
	}
	
	// Generation
	
	abstract fun getRawValue(x: Double, z: Double): Double
	
	fun getRawValue(x: Int, z: Int): Double{
		return getRawValue(x.toDouble(), z.toDouble())
	}
	
	inline fun getValue(x: Double, z: Double, block: NoiseValue.() -> Unit): Double{
		return NoiseValue(getRawValue(x, z)).apply(block).value
	}
	
	inline fun getValue(x: Int, z: Int, block: NoiseValue.() -> Unit): Double{
		return NoiseValue(getRawValue(x, z)).apply(block).value
	}
	
	// Implementations
	
	open class Perlin(rand: Random, private val xScale: Double, private val zScale: Double, octaves: Int) : NoiseGenerator(xScale, zScale){
		constructor(rand: Random, scale: Double, octaves: Int) : this(rand, scale, scale, octaves)
		
		private val generator = PerlinNoiseGenerator(rand, octaves)
		override fun getRawValue(x: Double, z: Double) = generator.getValue(x / xScale, z / zScale)
	}
	
	class PerlinNormalized(rand: Random, xScale: Double, zScale: Double, octaves: Int) : Perlin(rand, xScale, zScale, octaves){
		constructor(rand: Random, scale: Double, octaves: Int) : this(rand, scale, scale, octaves)
		
		private val normalizationApproxBoundary = ((1 shl octaves) - 1) * 0.998.pow(octaves)
		override fun getRawValue(x: Double, z: Double) = (super.getRawValue(x, z) + normalizationApproxBoundary) / (2 * normalizationApproxBoundary)
	}
}
