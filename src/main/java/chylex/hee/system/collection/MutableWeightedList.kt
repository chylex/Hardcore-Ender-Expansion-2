package chylex.hee.system.collection
import java.util.Random

class MutableWeightedList<T>(private val items: MutableList<Pair<Int, T>>){
	private val weightedList: WeightedList<T>
		get(){
			if (isDirty){
				cachedWeightedList = WeightedList(items)
				isDirty = false
			}
			
			return cachedWeightedList
		}
	
	private var cachedWeightedList = WeightedList(items)
	private var isDirty = false
	
	val values: List<T>
		get() = items.map { it.second }
	
	fun addItem(weight: Int, item: T){
		items.add(Pair(weight, item))
		isDirty = true
	}
	
	fun generateItem(rand: Random): T?{
		return if (items.isEmpty()) null else weightedList.generateItem(rand)
	}
	
	fun removeItem(rand: Random): T?{
		return if (items.isEmpty()) null else weightedList.generateEntry(rand).also { items.remove(it); isDirty = true }.second
	}
	
	override fun toString(): String{
		return items.toString()
	}
	
	companion object{
		fun <T> mutableWeightedListOf(vararg items: Pair<Int, T>): MutableWeightedList<T>{
			return MutableWeightedList(items.toMutableList())
		}
	}
}
