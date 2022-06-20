@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.util.math

import org.apache.commons.lang3.math.Fraction
import kotlin.math.ceil
import kotlin.math.floor

inline fun square(value: Int) = value * value
inline fun square(value: Float) = value * value
inline fun square(value: Double) = value * value

fun Float.floorToInt() = floor(this).toInt()
fun Float.ceilToInt() = ceil(this).toInt()

fun Double.floorToInt() = floor(this).toInt()
fun Double.ceilToInt() = ceil(this).toInt()

fun Float.toDegrees() = Math.toDegrees(this.toDouble()).toFloat()
fun Float.toRadians() = Math.toRadians(this.toDouble()).toFloat()

fun Double.toDegrees() = Math.toDegrees(this)
fun Double.toRadians() = Math.toRadians(this)

infix fun Int.over(denominator: Int): Fraction = Fraction.getFraction(this, denominator)

/**
 * Extremely necessary utility method to bit shift an [Int] into a [Long].
 */
infix fun Int.shlong(bitCount: Int): Long {
	return this.toLong() shl bitCount
}

/**
 * Linearly interpolates between [from] and [to], where [from] starts at [progress] = 0 and [to] ends at [progress] = 1.
 */
fun lerp(from: Float, to: Float, progress: Float): Float {
	return from + (to - from) * progress
}

/**
 * Linearly interpolates between [from] and [to], where [from] starts at [progress] = 0 and [to] ends at [progress] = 1.
 */
fun lerp(from: Double, to: Double, progress: Double): Double {
	return from + (to - from) * progress
}

/**
 * Remaps a value from the range [[fromMin], [fromMax]] to a value in the range [[toMin], [toMax]] using linear interpolation.
 */
fun Float.remap(fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
	val remappedBetween0And1 = (this - fromMin) / (fromMax - fromMin)
	return toMin + remappedBetween0And1 * (toMax - toMin)
}

/**
 * Remaps a value from the range [[fromMin], [fromMax]] to a value in the range [[toMin], [toMax]] using linear interpolation.
 */
fun Double.remap(fromMin: Double, fromMax: Double, toMin: Double, toMax: Double): Double {
	val remappedBetween0And1 = (this - fromMin) / (fromMax - fromMin)
	return toMin + remappedBetween0And1 * (toMax - toMin)
}
