package chylex.hee.game.world.generation.noise

import net.minecraft.util.SharedSeedRandom
import net.minecraft.world.gen.PerlinNoiseGenerator
import net.minecraft.world.gen.SimplexNoiseGenerator
import java.util.Random
import java.util.stream.IntStream
import kotlin.math.pow

sealed class Noise2D(private val xScale: Double, private val zScale: Double) {
	
	// Generation
	
	abstract fun getRawValue(x: Double, z: Double): Double
	
	fun getRawValue(x: Int, z: Int): Double {
		return getRawValue(x.toDouble(), z.toDouble())
	}
	
	inline fun getValue(x: Double, z: Double, block: NoiseValue.() -> Unit): Double {
		return NoiseValue(getRawValue(x, z)).apply(block).value
	}
	
	inline fun getValue(x: Int, z: Int, block: NoiseValue.() -> Unit): Double {
		return NoiseValue(getRawValue(x, z)).apply(block).value
	}
	
	// Implementations
	
	open class OldPerlin(rand: Random, private val xScale: Double, private val zScale: Double, octaves: Int) : Noise2D(xScale, zScale) {
		constructor(rand: Random, scale: Double, octaves: Int) : this(rand, scale, scale, octaves)
		
		private val noiseLevels = Array(octaves) { SimplexNoiseGenerator(rand) }
		
		override fun getRawValue(x: Double, z: Double): Double {
			val scaledX = x / xScale
			val scaledZ = z / zScale
			
			var value = 0.0
			var mp = 1.0
			
			for (level in noiseLevels) {
				value += level.getValue(scaledX * mp, scaledZ * mp) / mp
				mp /= 2.0
			}
			
			return value
		}
	}
	
	class OldPerlinNormalized(rand: Random, xScale: Double, zScale: Double, octaves: Int) : OldPerlin(rand, xScale, zScale, octaves) {
		constructor(rand: Random, scale: Double, octaves: Int) : this(rand, scale, scale, octaves)
		
		private val normalizationApproxBoundary = ((1 shl octaves) - 1) * 0.998.pow(octaves)
		override fun getRawValue(x: Double, z: Double) = (super.getRawValue(x, z) + normalizationApproxBoundary) / (2 * normalizationApproxBoundary)
	}
	
	class NewPerlin(rand: Random, private val xScale: Double, private val zScale: Double, octaves: Int) : Noise2D(xScale, zScale) {
		constructor(rand: Random, scale: Double, octaves: Int) : this(rand, scale, scale, octaves)
		
		private val generator = PerlinNoiseGenerator(SharedSeedRandom(rand.nextLong()), IntStream.rangeClosed(-octaves + 1, 0))
		override fun getRawValue(x: Double, z: Double) = generator.noiseAt(x / xScale, z / zScale, false)
	}
}
