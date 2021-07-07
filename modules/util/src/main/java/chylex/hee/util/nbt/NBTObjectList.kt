package chylex.hee.util.nbt

import net.minecraft.nbt.INBT

class NBTObjectList<T : Any>(tagList: TagList = TagList()) : NBTList<T>(tagList) {
	override fun convert(element: T): INBT = when (element) {
		is TagCompound -> element
		is String      -> TagString.valueOf(element)
		is ByteArray   -> TagByteArray(element)
		is IntArray    -> TagIntArray(element)
		is LongArray   -> TagLongArray(element)
		else           -> throw IllegalArgumentException("unhandled NBT type conversion: ${element::class.java.simpleName}")
	}
	
	override fun get(index: Int): T {
		val tag = tagList[index]
		
		@Suppress("UNCHECKED_CAST")
		return when (tag) {
			is TagCompound  -> tag as T
			is TagString    -> tag.string as T
			is TagByteArray -> tag.byteArray as T
			is TagIntArray  -> tag.intArray as T
			is TagLongArray -> tag.asLongArray as T
			is TagEnd       -> throw IndexOutOfBoundsException()
			else            -> throw IllegalArgumentException("unhandled NBT type conversion: ${tag::class.java.simpleName}")
		}
	}
	
	companion object {
		@JvmName("ofCompounds")  fun of(elements: Iterable<TagCompound>) = NBTObjectList<TagCompound>().apply { elements.forEach(::append) }
		@JvmName("ofStrings")    fun of(elements: Iterable<String>)      = NBTObjectList<String>().apply      { elements.forEach(::append) }
		@JvmName("ofByteArrays") fun of(elements: Iterable<ByteArray>)   = NBTObjectList<ByteArray>().apply   { elements.forEach(::append) }
		@JvmName("ofIntArrays")  fun of(elements: Iterable<IntArray>)    = NBTObjectList<IntArray>().apply    { elements.forEach(::append) }
		@JvmName("ofLongArrays") fun of(elements: Iterable<LongArray>)   = NBTObjectList<LongArray>().apply   { elements.forEach(::append) }
	}
}
