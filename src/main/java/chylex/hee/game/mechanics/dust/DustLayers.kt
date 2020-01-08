package chylex.hee.game.mechanics.dust
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.size
import net.minecraft.item.ItemStack
import kotlin.math.min

class DustLayers(val totalCapacity: Int){
	private companion object{
		private const val TYPE_TAG = "Type"
		private const val AMOUNT_TAG = "Amount"
	}
	
	enum class Side{
		TOP{
			override fun index(list: List<Pair<DustType, Short>>) = if (list.isEmpty()) null else list.lastIndex
		},
		BOTTOM{
			override fun index(list: List<Pair<DustType, Short>>) = if (list.isEmpty()) null else 0
		};
		
		abstract fun index(list: List<Pair<DustType, Short>>): Int?
	}
	
	private val layers = mutableListOf<Pair<DustType, Short>>()
	private val listeners = mutableListOf<() -> Unit>()
	
	val contents: List<Pair<DustType, Short>>
		get() = layers
	
	val remainingCapacity
		get() = totalCapacity - layers.sumBy { it.second.toInt() }
	
	// Update handling
	
	fun onUpdate(callback: () -> Unit){
		listeners.add(callback)
	}
	
	private fun triggerUpdate(){
		listeners.forEach { it.invoke() }
	}
	
	// Dust handling
	
	fun addDust(type: DustType, amount: Int): Int{
		val added = min(amount, remainingCapacity)
		
		if (added > 0){
			val top = layers.lastOrNull()
			
			if (top != null && top.first === type){
				layers[layers.lastIndex] = type to (top.second + added).toShort()
			}
			else{
				layers.add(type to added.toShort())
			}
			
			triggerUpdate()
		}
		
		return added
	}
	
	fun getDustType(side: Side): DustType?{
		return side.index(layers)?.let(layers::get)?.first
	}
	
	fun getDust(side: Side, amount: Int = Int.MAX_VALUE): ItemStack{
		val (dustType, dustAmount) = side.index(layers)?.let(layers::get) ?: return ItemStack.EMPTY
		
		val total = minOf(amount, dustType.maxStackSize, dustAmount.toInt())
		return ItemStack(dustType.item, total)
	}
	
	fun removeDust(side: Side, type: DustType, amount: Int = Int.MAX_VALUE): ItemStack{
		return if (side.index(layers)?.let(layers::get)?.first != type)
			ItemStack.EMPTY
		else
			removeDust(side, amount)
	}
	
	fun removeDust(side: Side, amount: Int = Int.MAX_VALUE): ItemStack{
		val index = side.index(layers) ?: return ItemStack.EMPTY
		val (dustType, dustAmount) = layers[index]
		
		val removed = getDust(side, amount)
		
		if (removed.size < dustAmount){
			layers[index] = dustType to (dustAmount - removed.size).toShort()
		}
		else{
			layers.removeAt(index)
		}
		
		triggerUpdate()
		return removed
	}
	
	// Serialization
	
	fun serializeNBT() = NBTObjectList<TagCompound>().apply {
		for((dustType, dustAmount) in layers){
			append(TagCompound().also {
				it.putString(TYPE_TAG, dustType.key)
				it.putShort(AMOUNT_TAG, dustAmount)
			})
		}
	}
	
	fun deserializeNBT(nbt: NBTObjectList<TagCompound>) = with(nbt){
		layers.clear()
		
		for(tag in this){
			val dustType = tag.getString(TYPE_TAG)
			val dustAmount = tag.getShort(AMOUNT_TAG)
			
			DustType.values().firstOrNull { it.key == dustType }?.let { it to dustAmount }?.let(layers::add)
		}
	}
}
