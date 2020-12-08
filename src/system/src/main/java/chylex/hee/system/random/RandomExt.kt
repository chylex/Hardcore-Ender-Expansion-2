package chylex.hee.system.random
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.floorToInt
import net.minecraft.util.math.Vec3d
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow

/**
 * Returns a random long integer between `min` and `max` (both inclusive).
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextLong(bound: Long): Long{
	require(bound > 0){ "bound must be positive" }
	return abs(this.nextLong()) % bound
}

/**
 * Returns a random integer between `min` and `max` (both inclusive).
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextInt(min: Int, max: Int): Int{
	require(min <= max){ "min must be smaller than or equal to max" }
	return min + this.nextInt(max - min + 1)
}

/**
 * Returns a random integer within the specified range.
 */
fun Random.nextInt(range: IntRange): Int{
	return this.nextInt(range.first, range.last)
}

/**
 * Returns a random long integer between `min` and `max` (both inclusive).
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextLong(min: Long, max: Long): Long{
	require(min <= max){ "min must be smaller than or equal to max" }
	return min + this.nextLong(max - min + 1)
}

/**
 * Returns a random floating point number between `min` and `max`.
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextFloat(min: Float, max: Float): Float{
	require(min <= max){ "min must be smaller than or equal to max" }
	return min + (this.nextFloat() * (max - min))
}

/**
 * Returns a random floating point number within the specified range.
 */
fun Random.nextFloat(range: ClosedFloatingPointRange<Float>): Float{
	return this.nextFloat(range.start, range.endInclusive)
}

/**
 * Returns a random floating point number between `min` and `max`, but returns a [Double] for convenience.
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextFloat(min: Double, max: Double): Double{
	require(min <= max){ "min must be smaller than or equal to max" }
	return min + (this.nextFloat() * (max - min))
}

/**
 * Returns a random floating point number within the specified range, but returns a [Double] for convenience.
 */
fun Random.nextFloat(range: ClosedFloatingPointRange<Double>): Double{
	return this.nextFloat(range.start, range.endInclusive)
}

/**
 * Uses the decimal part of a number as a probability of rounding the number up.
 * For example, when the provided value is 3.8, the function will have an 80% chance to return 4, and a 20% chance to return 3.
 */
fun Random.nextRounded(value: Float): Int{
	val decimalPart = value - value.toInt()
	return if (this.nextFloat() < decimalPart) value.ceilToInt() else value.floorToInt()
}

/**
 * Returns a random floating point number between 0 and 1, with bias towards lower values.
 * @param[biasSoftener] represents how much the bias will be reduced, starting at 1 for full bias, and ending at theoretical infinity for no bias
 * @throws IllegalArgumentException if `biasSoftener` is lower than 1
 */
fun Random.nextBiasedFloat(biasSoftener: Float): Float{
	require(biasSoftener >= 1F){ "biasSoftener must be at least 1" }
	
	val unbiased = this.nextFloat()
	return unbiased - (unbiased * this.nextFloat().pow(biasSoftener))
}

/**
 * Returns a random item from the list while also removing it from the list, or throws if the list is empty.
 */
fun <T> Random.removeItem(collection: MutableList<T>): T{
	val size = collection.size
	return if (size == 0) throw NoSuchElementException() else collection.removeAt(this.nextInt(size))
}

/**
 * Returns a random item from the list while also removing it from the list, or returns `null` if the list is empty.
 */
fun <T> Random.removeItemOrNull(collection: MutableList<T>): T?{
	val size = collection.size
	return if (size == 0) null else collection.removeAt(this.nextInt(size))
}

/**
 * Returns a random item from the list, or `null` if the list is empty.
 */
fun <T> Random.nextItemOrNull(collection: List<T>): T?{
	val size = collection.size
	return if (size == 0) null else collection[this.nextInt(size)]
}

/**
 * Returns a random item from the array, or `null` if the array is empty.
 */
fun <T> Random.nextItemOrNull(collection: Array<T>): T?{
	return if (collection.isEmpty()) null else collection[this.nextInt(collection.size)]
}

/**
 * Returns a random item from the list, or throws if the list is empty.
 */
fun <T> Random.nextItem(collection: List<T>): T{
	val size = collection.size
	return if (size == 0) throw NoSuchElementException() else collection[this.nextInt(size)]
}

/**
 * Returns a random item from the array, or throws if the array is empty.
 */
fun <T> Random.nextItem(collection: Array<T>): T{
	return if (collection.isEmpty()) throw NoSuchElementException() else collection[this.nextInt(collection.size)]
}

/**
 * Returns a random item from the enum, or throws if the enum has no values.
 */
inline fun <reified T : Enum<T>> Random.nextItem(): T{
	return this.nextItem(enumValues())
}

/**
 * Returns a random integer from the array, or the provided default value if the array is empty.
 */
fun Random.nextItem(collection: IntArray, default: Int): Int{
	return if (collection.isEmpty()) default else collection[this.nextInt(collection.size)]
}

/**
 * Returns a random point inside a sphere, represented by a vector.
 */
fun Random.nextVector(scale: Double): Vec3d{
	return Vec(this.nextDouble() * 2.0 - 1.0, this.nextDouble() * 2.0 - 1.0, this.nextDouble() * 2.0 - 1.0).normalize().scale(scale)
}

/**
 * Returns a random point inside a circle, represented by the XZ components of a vector, with its Y component equal to [y].
 */
fun Random.nextVector2(xz: Double, y: Double): Vec3d{
	return Vec3.xz(this.nextDouble() * 2.0 - 1.0, this.nextDouble() * 2.0 - 1.0).normalize().scale(xz).addY(y)
}
