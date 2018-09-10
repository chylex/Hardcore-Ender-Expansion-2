package chylex.hee.system.collection
import chylex.hee.system.util.nextLong
import java.util.Random

class WeightedList<T>(private val items: List<Pair<Int, T>>){
	private val totalWeight =
		if (items.isEmpty())
			throw IllegalArgumentException("weighted list must not be empty")
		else
			items.fold(0L){ total, item -> if (item.first > 0) total + item.first else throw IllegalArgumentException("weight must be > 0") }
	
	fun generateItem(rand: Random): T{
		var remaining = rand.nextLong(totalWeight)
		
		for(item in items){
			remaining -= item.first
			
			if (remaining < 0){
				return item.second
			}
		}
		
		throw IllegalStateException("failed generating a random weighted item")
	}
	
	companion object{
		fun <T> weightedListOf(vararg items: Pair<Int, T>): WeightedList<T>{
			return WeightedList(items.toList())
		}
	}
}
