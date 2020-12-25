@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.math

import org.apache.commons.lang3.math.Fraction
import kotlin.math.ceil
import kotlin.math.floor

fun Float.floorToInt() = floor(this).toInt()
fun Float.ceilToInt() = ceil(this).toInt()

fun Double.floorToInt() = floor(this).toInt()
fun Double.ceilToInt() = ceil(this).toInt()

inline fun square(value: Int) = value * value
inline fun square(value: Float) = value * value
inline fun square(value: Double) = value * value

inline fun Float.toDegrees() = Math.toDegrees(this.toDouble()).toFloat()
inline fun Float.toRadians() = Math.toRadians(this.toDouble()).toFloat()

inline fun Double.toDegrees() = Math.toDegrees(this)
inline fun Double.toRadians() = Math.toRadians(this)

infix fun Int.over(denominator: Int): Fraction = Fraction.getFraction(this, denominator)

/**
 * Extremely necessary utility method to bit shift an [Int] into a [Long].
 */
infix fun Int.shlong(bitCount: Int): Long {
	return this.toLong() shl bitCount
}

fun offsetTowards(from: Float, to: Float, progress: Float): Float {
	return from + (to - from) * progress
}

fun offsetTowards(from: Double, to: Double, progress: Double): Double {
	return from + (to - from) * progress
}

/**
 * Maps a range of values in [from] range to values in [to] range using linear interpolation.
 */
fun remapRange(value: Float, from: ClosedFloatingPointRange<Float>, to: ClosedFloatingPointRange<Float>): Float {
	val remappedBetween0And1 = (value - from.start) / (from.endInclusive - from.start)
	return to.start + remappedBetween0And1 * (to.endInclusive - to.start)
}

/**
 * Maps a range of values in [from] range to values in [to] range using linear interpolation.
 */
fun remapRange(value: Double, from: ClosedFloatingPointRange<Double>, to: ClosedFloatingPointRange<Double>): Double {
	val remappedBetween0And1 = (value - from.start) / (from.endInclusive - from.start)
	return to.start + remappedBetween0And1 * (to.endInclusive - to.start)
}
