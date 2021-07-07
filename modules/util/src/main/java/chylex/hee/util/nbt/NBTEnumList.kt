package chylex.hee.util.nbt

import java.util.Locale

class NBTEnumList<T : Enum<T>>(private val cls: Class<T>, tagList: TagList) : NBTList<T>(tagList) {
	private constructor(cls: Class<T>) : this(cls, TagList())
	
	companion object {
		fun <T : Enum<T>> of(cls: Class<T>, elements: Iterable<T>) = NBTEnumList(cls).apply { elements.forEach(::append) }
		inline fun <reified T : Enum<T>> of(elements: Iterable<T>) = of(T::class.java, elements)
	}
	
	override fun convert(element: T): NBTBase {
		return TagString.valueOf(element.name.lowercase(Locale.ROOT))
	}
	
	override fun get(index: Int): T {
		return java.lang.Enum.valueOf(cls, tagList.getString(index).uppercase(Locale.ROOT))
	}
}
