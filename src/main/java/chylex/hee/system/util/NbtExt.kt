package chylex.hee.system.util
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTPrimitive
import net.minecraft.nbt.NBTTagByte
import net.minecraft.nbt.NBTTagByteArray
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagDouble
import net.minecraft.nbt.NBTTagEnd
import net.minecraft.nbt.NBTTagFloat
import net.minecraft.nbt.NBTTagInt
import net.minecraft.nbt.NBTTagIntArray
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagLong
import net.minecraft.nbt.NBTTagLongArray
import net.minecraft.nbt.NBTTagShort
import net.minecraft.nbt.NBTTagString
import net.minecraftforge.common.util.Constants.NBT
import java.util.Locale

// Items

/**
 * Returns the ItemStack's NBT tag. If the ItemStack has no tag, it will be created.
 */
inline val ItemStack.nbt: NBTTagCompound
	get() = this.tagCompound ?: NBTTagCompound().also { this.tagCompound = it }

/**
 * Returns the ItemStack's NBT tag. If the ItemStack has no tag, null is returned instead.
 */
inline val ItemStack.nbtOrNull: NBTTagCompound?
	get() = this.tagCompound

// Enums

inline fun <reified T : Enum<T>> NBTTagCompound.setEnum(key: String, value: T?){
	setString(key, value?.name?.toLowerCase(Locale.ROOT) ?: "")
}

inline fun <reified T : Enum<T>> NBTTagCompound.getEnum(key: String): T?{
	val value = getString(key)
	
	return if (value.isEmpty())
		null
	else
		java.lang.Enum.valueOf(T::class.java, value.toUpperCase(Locale.ROOT))
}

// Lists

fun NBTTagCompound.getListOfPrimitives(key: String): NBTPrimitiveList{
	val tag = this.getTag(key)
	
	return if (tag is NBTTagList && (tag.hasNoTags() || tag.get(0) is NBTPrimitive)){
		NBTPrimitiveList(tag)
	}
	else{
		NBTPrimitiveList(NBTTagList())
	}
}

fun NBTTagCompound.getListOfCompounds(key: String)  = NBTObjectList<NBTTagCompound>(this.getTagList(key, NBT.TAG_COMPOUND))
fun NBTTagCompound.getListOfStrings(key: String)    = NBTObjectList<String>(this.getTagList(key, NBT.TAG_STRING))
fun NBTTagCompound.getListOfByteArrays(key: String) = NBTObjectList<ByteArray>(this.getTagList(key, NBT.TAG_BYTE_ARRAY))
fun NBTTagCompound.getListOfIntArrays(key: String)  = NBTObjectList<IntArray>(this.getTagList(key, NBT.TAG_INT_ARRAY))
// UPDATE: see below | fun NBTTagCompound.getListOfLongArrays(key: String) = NBTObjectList<LongArray>(this.getTagList(key, NBT.TAG_LONG_ARRAY))

abstract class NBTList<T : Any>(protected val tagList: NBTTagList) : Iterable<T>{
	companion object{
		fun <T : Any> NBTTagCompound.setList(key: String, list: NBTList<T>){
			this.setTag(key, list.tagList)
		}
	}
	
	val size
		get() = tagList.tagCount()
	
	val isEmpty
		get() = tagList.hasNoTags()
	
	abstract fun append(element: T)
	abstract fun get(index: Int) : T
	
	override fun iterator(): MutableIterator<T> = object : MutableIterator<T>{
		private var cursor: Int = 0
		private var canRemove: Boolean = false
		
		override fun hasNext(): Boolean{
			return cursor != size
		}
		
		override fun next(): T{
			if (cursor >= size){
				throw NoSuchElementException()
			}
			
			canRemove = true
			return get(cursor++)
		}
		
		override fun remove(){
			if (!canRemove){
				throw IllegalStateException()
			}
			
			canRemove = false
			tagList.removeTag(--cursor)
		}
	}
	
	override fun equals(other: Any?): Boolean = tagList == other
	
	override fun hashCode(): Int = tagList.hashCode()
	
	override fun toString(): String = tagList.toString()
}

class NBTPrimitiveList(tagList: NBTTagList) : NBTList<NBTPrimitive>(tagList){
	val allBytes
		get() = iterator().asSequence().map(NBTPrimitive::getByte)
	
	val allShorts
		get() = iterator().asSequence().map(NBTPrimitive::getShort)
	
	val allInts
		get() = iterator().asSequence().map(NBTPrimitive::getInt)
	
	val allLongs
		get() = iterator().asSequence().map(NBTPrimitive::getLong)
	
	val allFloats
		get() = iterator().asSequence().map(NBTPrimitive::getFloat)
	
	val allDoubles
		get() = iterator().asSequence().map(NBTPrimitive::getDouble)
	
	fun append(value: Byte)   = tagList.appendTag(NBTTagByte(value))
	fun append(value: Short)  = tagList.appendTag(NBTTagShort(value))
	fun append(value: Int)    = tagList.appendTag(NBTTagInt(value))
	fun append(value: Long)   = tagList.appendTag(NBTTagLong(value))
	fun append(value: Float)  = tagList.appendTag(NBTTagFloat(value))
	fun append(value: Double) = tagList.appendTag(NBTTagDouble(value))
	
	override fun append(element: NBTPrimitive){
		tagList.appendTag(element)
	}
	
	override fun get(index: Int): NBTPrimitive{
		val tag = tagList.get(index)
		
		return when(tag){
			is NBTPrimitive -> tag
			is NBTTagEnd    -> throw IndexOutOfBoundsException()
			else            -> throw IllegalArgumentException("unhandled NBT type: ${tag::class.java.simpleName}")
		}
	}
}

class NBTObjectList<T : Any>(tagList: NBTTagList) : NBTList<T>(tagList){
	override fun append(element: T){
		tagList.appendTag(when(element){
			is NBTTagCompound -> element
			is String         -> NBTTagString(element)
			is ByteArray      -> NBTTagByteArray(element)
			is IntArray       -> NBTTagIntArray(element)
			is LongArray      -> NBTTagLongArray(element)
			else              -> throw IllegalArgumentException("unknown NBT type: ${element::class.java.simpleName}")
		})
	}
	
	override fun get(index: Int): T{
		val tag = tagList.get(index)
		
		@Suppress("UNCHECKED_CAST")
		return when(tag){
			is NBTTagCompound  -> tag as T
			is NBTTagString    -> tag.string as T
			is NBTTagByteArray -> tag.byteArray as T
			is NBTTagIntArray  -> tag.intArray as T
			// UPDATE: check if this is available | is NBTTagLongArray -> tag.longArray as T
			is NBTTagEnd       -> throw IndexOutOfBoundsException()
			else               -> throw IllegalArgumentException("unhandled NBT type: ${tag::class.java.simpleName}")
		}
	}
}
