package chylex.hee.system.util
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

infix fun Int.over(denominator: Int): Fraction = Fraction.getFraction(this, denominator)
