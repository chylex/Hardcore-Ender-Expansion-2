package chylex.hee.util.nbt

import net.minecraftforge.common.util.Constants.NBT

abstract class NBTList<T : Any>(internal val tagList: TagList) : Iterable<T> {
	val size
		get() = tagList.size
	
	val isEmpty
		get() = tagList.isEmpty()
	
	protected abstract fun convert(element: T): NBTBase
	
	open fun append(element: T) {
		tagList.add(convert(element))
	}
	
	open fun set(index: Int, element: T) {
		tagList[index] = convert(element)
	}
	
	abstract fun get(index: Int): T
	
	override fun iterator() = object : MutableIterator<T> {
		private var cursor = 0
		private var canRemove = false
		
		override fun hasNext(): Boolean {
			return cursor != size
		}
		
		override fun next(): T {
			if (cursor >= size) {
				throw NoSuchElementException()
			}
			
			canRemove = true
			return get(cursor++)
		}
		
		override fun remove() {
			check(canRemove)
			
			canRemove = false
			tagList.removeAt(--cursor)
		}
	}
	
	override fun equals(other: Any?) = tagList == other
	override fun hashCode() = tagList.hashCode()
	override fun toString() = tagList.toString()
}

fun <T : Any> TagCompound.putList(key: String, list: NBTList<T>) {
	this.put(key, list.tagList)
}

fun TagCompound.getListOfPrimitives(key: String): NBTPrimitiveList {
	val tag = this.get(key)
	
	return if (tag is TagList && (tag.isEmpty() || tag[0] is NBTPrimitive))
		NBTPrimitiveList(tag)
	else
		NBTPrimitiveList(TagList())
}

fun TagCompound.getListOfCompounds(key: String)  = NBTObjectList<TagCompound>(this.getList(key, NBT.TAG_COMPOUND))
fun TagCompound.getListOfStrings(key: String)    = NBTObjectList<String>(this.getList(key, NBT.TAG_STRING))
fun TagCompound.getListOfByteArrays(key: String) = NBTObjectList<ByteArray>(this.getList(key, NBT.TAG_BYTE_ARRAY))
fun TagCompound.getListOfIntArrays(key: String)  = NBTObjectList<IntArray>(this.getList(key, NBT.TAG_INT_ARRAY))
fun TagCompound.getListOfLongArrays(key: String) = NBTObjectList<LongArray>(this.getList(key, NBT.TAG_LONG_ARRAY))

fun TagCompound.getListOfItemStacks(key: String) = NBTItemStackList(this.getList(key, NBT.TAG_COMPOUND))

inline fun <reified T : Enum<T>> TagCompound.getListOfEnums(key: String) = NBTEnumList(T::class.java, this.getList(key, NBT.TAG_STRING))
