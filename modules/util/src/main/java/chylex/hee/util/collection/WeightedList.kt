package chylex.hee.util.collection

import chylex.hee.util.random.nextLong
import java.util.Random

class WeightedList<T>(val items: List<Pair<Int, T>>) {
	private val totalWeight =
		if (items.isEmpty())
			throw IllegalArgumentException("weighted list must not be empty")
		else
			items.fold(0L) { total, (weight, _) -> if (weight > 0) total + weight else throw IllegalArgumentException("weight must be > 0") }
	
	val values: List<T>
		get() = items.map { it.second }
	
	fun generateEntry(rand: Random): Pair<Int, T> {
		var remaining = rand.nextLong(totalWeight)
		
		for (item in items) {
			remaining -= item.first
			
			if (remaining < 0) {
				return item
			}
		}
		
		throw IllegalStateException("failed generating a random weighted item")
	}
	
	fun generateItem(rand: Random): T {
		return generateEntry(rand).second
	}
	
	fun mutableCopy(): MutableWeightedList<T> {
		return MutableWeightedList(items.toMutableList())
	}
	
	override fun toString(): String {
		return items.toString()
	}
}

fun <T> weightedListOf(vararg items: Pair<Int, T>): WeightedList<T> {
	return WeightedList(items.toList())
}
