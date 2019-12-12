package chylex.hee.system.collection
import java.util.Random

class MutableWeightedList<T>(private val items: MutableList<Pair<Int, T>>){
	private val weightedList: WeightedList<T>?
		get(){
			if (isDirty){
				cachedWeightedList = items.ifEmpty { null }?.let { WeightedList(it) }
				isDirty = false
			}
			
			return cachedWeightedList
		}
	
	private var cachedWeightedList: WeightedList<T>? = null
	private var isDirty = true
	
	val values: List<T>
		get() = items.map { it.second }
	
	fun addItem(weight: Int, item: T){
		items.add(Pair(weight, item))
		isDirty = true
	}
	
	fun addItems(list: WeightedList<T>){
		items.addAll(list.items)
		isDirty = true
	}
	
	fun addItems(list: MutableWeightedList<T>){
		items.addAll(list.items)
		isDirty = true
	}
	
	fun generateItem(rand: Random): T?{
		return weightedList?.generateItem(rand)
	}
	
	fun removeItem(rand: Random): T?{
		return weightedList?.let { list -> list.generateEntry(rand).also { items.remove(it); isDirty = true }.second }
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
