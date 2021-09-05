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
 * Maps a range of values in [from] range to values in [to] range using linear interpolation.
 */
fun remapRange(value: Float, from: FloatRange, to: FloatRange): Float {
	val remappedBetween0And1 = (value - from.start) / (from.end - from.start)
	return to.start + remappedBetween0And1 * (to.end - to.start)
}

/**
 * Maps a range of values in [from] range to values in [to] range using linear interpolation.
 */
fun remapRange(value: Double, from: FloatRange, to: FloatRange): Double {
	val remappedBetween0And1 = (value - from.start) / (from.end - from.start)
	return to.start + remappedBetween0And1 * (to.end - to.start)
}
