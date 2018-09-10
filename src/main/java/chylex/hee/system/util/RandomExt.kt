package chylex.hee.system.util
import java.util.Random
import kotlin.math.abs

/**
 * Returns a random long integer between `min` and `max` (both inclusive).
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextLong(bound: Long): Long{
	if (bound <= 0){
		throw IllegalArgumentException("bound must be positive")
	}
	
	return abs(this.nextLong()) % bound
}

/**
 * Returns a random integer between `min` and `max` (both inclusive).
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextInt(min: Int, max: Int): Int{
	if (min > max){
		throw IllegalArgumentException("min must be smaller than or equal to max")
	}
	
	return min + this.nextInt(max - min + 1)
}

/**
 * Returns a random long integer between `min` and `max` (both inclusive).
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextLong(min: Long, max: Long): Long{
	if (min > max){
		throw IllegalArgumentException("min must be smaller than or equal to max")
	}
	
	return min + this.nextLong(max - min + 1)
}

/**
 * Returns a random floating point number between `min` and `max`.
 * @throws IllegalArgumentException if `min` is greater than `max`
 */
fun Random.nextFloat(min: Float, max: Float): Float{
	if (min > max){
		throw IllegalArgumentException("min must be smaller than or equal to max")
	}
	
	return min + (this.nextFloat() * (max - min))
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
 * Returns a random item from the list, or `null` if the list is empty.
 */
fun <T> Random.nextItem(collection: List<T>): T?{
	val size = collection.size
	return if (size == 0) null else collection[this.nextInt(size)]
}

/**
 * Returns a random item from the array, or `null` if the array is empty.
 */
fun <T> Random.nextItem(collection: Array<T>): T?{
	return if (collection.isEmpty()) null else collection[this.nextInt(collection.size)]
}

/**
 * Returns a random integer from the array, or the provided default value if the array is empty.
 */
fun Random.nextItem(collection: IntArray, default: Int): Int{
	return if (collection.isEmpty()) default else collection[this.nextInt(collection.size)]
}
