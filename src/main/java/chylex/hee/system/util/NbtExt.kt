@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.util
import chylex.hee.HEE
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.Constants.NBT
import org.apache.commons.lang3.ArrayUtils.EMPTY_LONG_ARRAY
import java.util.Locale
import java.util.UUID
import kotlin.contracts.contract

typealias NBTBase      = net.minecraft.nbt.NBTBase
typealias NBTPrimitive = net.minecraft.nbt.NBTPrimitive
typealias TagByte      = net.minecraft.nbt.NBTTagByte
typealias TagByteArray = net.minecraft.nbt.NBTTagByteArray
typealias TagCompound  = net.minecraft.nbt.NBTTagCompound
typealias TagDouble    = net.minecraft.nbt.NBTTagDouble
typealias TagEnd       = net.minecraft.nbt.NBTTagEnd
typealias TagFloat     = net.minecraft.nbt.NBTTagFloat
typealias TagInt       = net.minecraft.nbt.NBTTagInt
typealias TagIntArray  = net.minecraft.nbt.NBTTagIntArray
typealias TagList      = net.minecraft.nbt.NBTTagList
typealias TagLong      = net.minecraft.nbt.NBTTagLong
typealias TagLongArray = net.minecraft.nbt.NBTTagLongArray
typealias TagShort     = net.minecraft.nbt.NBTTagShort
typealias TagString    = net.minecraft.nbt.NBTTagString

fun TagCompound.getOrCreateCompound(key: String): TagCompound{
	return if (this.hasKey(key))
		this.getCompoundTag(key)
	else
		TagCompound().also { setTag(key, it) }
}

// HEE tag

private const val HEE_TAG_NAME = HEE.ID

val TagCompound.heeTag
	get() = this.getOrCreateCompound(HEE_TAG_NAME)

val TagCompound.heeTagOrNull
	get() = this.getCompoundOrNull(HEE_TAG_NAME)

// ItemStacks

inline fun TagCompound.writeStack(stack: ItemStack){
	if (stack.isNotEmpty){
		stack.writeToNBT(this)
	}
}

inline fun TagCompound.readStack(): ItemStack{
	return if (this.size == 0)
		ItemStack.EMPTY
	else
		ItemStack(this)
}

fun TagCompound.setStack(key: String, stack: ItemStack){
	this.setTag(key, TagCompound().also { it.writeStack(stack) })
}

fun TagCompound.getStack(key: String): ItemStack{
	return this.getCompoundTag(key).readStack()
}

// Inventories

private const val SLOT_TAG = "Slot"

fun TagCompound.saveInventory(key: String, inventory: IInventory){
	val list = TagList()
	
	for((slot, stack) in inventory.nonEmptySlots){
		list.appendTag(TagCompound().also {
			stack.writeToNBT(it)
			it.setInteger(SLOT_TAG, slot)
		})
	}
	
	this.setTag(key, list)
}

fun TagCompound.loadInventory(key: String, inventory: IInventory){
	inventory.clear()
	
	for(tag in this.getListOfCompounds(key)){
		inventory.setStack(tag.getInteger(SLOT_TAG), ItemStack(tag))
	}
}

// Long arrays

fun TagCompound.setLongArray(key: String, array: LongArray){
	this.setTag(key, TagLongArray(array))
}

fun TagCompound.getLongArray(key: String): LongArray{
	return if (this.hasKey(key, NBT.TAG_LONG_ARRAY))
		(this.getTag(key) as? TagLongArray)?.data ?: EMPTY_LONG_ARRAY
	else
		EMPTY_LONG_ARRAY
}

// BlockPos

inline fun TagCompound.setPos(key: String, pos: BlockPos){
	this.setLong(key, pos.toLong())
}

fun TagCompound.getPos(key: String): BlockPos{
	return if (this.hasKey(key, NBT.TAG_LONG))
		Pos(this.getLong(key))
	else
		BlockPos.ORIGIN
}

// UUID

inline fun TagCompound.hasUUID(key: String): Boolean{
	return this.hasUniqueId(key)
}

inline fun TagCompound.getUUID(key: String): UUID{
	return this.getUniqueId(key)!! // UPDATE marked as Nullable, but can never actually return null
}

inline fun TagCompound.setUUID(key: String, uuid: UUID){
	this.setUniqueId(key, uuid)
}

// Enums

inline fun <reified T : Enum<T>> TagCompound.setEnum(key: String, value: T?){
	this.setString(key, value?.name?.toLowerCase(Locale.ROOT) ?: "")
}

inline fun <reified T : Enum<T>> TagCompound.getEnum(key: String): T?{
	val value = this.getString(key)
	
	return if (value.isEmpty())
		null
	else
		try{ java.lang.Enum.valueOf(T::class.java, value.toUpperCase(Locale.ROOT)) }catch(e: IllegalArgumentException){ null }
}

// Presence checks

inline fun TagCompound?.hasKey(key: String): Boolean{
	contract { returns(true) implies (this@hasKey != null) }
	return this != null && this.hasKey(key)
}

inline fun TagCompound?.hasKey(key: String, type: Int): Boolean{
	contract { returns(true) implies (this@hasKey != null) }
	return this != null && this.hasKey(key, type)
}

inline fun <T> TagCompound?.ifPresent(key: String, type: Int, getter: (String) -> T): T?{
	return if (this.hasKey(key, type))
		getter(key)
	else
		null
}

fun TagCompound.getByteOrNull(key: String): Byte?            = ifPresent(key, NBT.TAG_BYTE, ::getByte)
fun TagCompound.getShortOrNull(key: String): Short?          = ifPresent(key, NBT.TAG_SHORT, ::getShort)
fun TagCompound.getIntegerOrNull(key: String): Int?          = ifPresent(key, NBT.TAG_INT, ::getInteger)
fun TagCompound.getLongOrNull(key: String): Long?            = ifPresent(key, NBT.TAG_LONG, ::getLong)
fun TagCompound.getFloatOrNull(key: String): Float?          = ifPresent(key, NBT.TAG_FLOAT, ::getFloat)
fun TagCompound.getDoubleOrNull(key: String): Double?        = ifPresent(key, NBT.TAG_DOUBLE, ::getDouble)
fun TagCompound.getStringOrNull(key: String): String?        = ifPresent(key, NBT.TAG_STRING, ::getString)
fun TagCompound.getCompoundOrNull(key: String): TagCompound? = ifPresent(key, NBT.TAG_COMPOUND, ::getCompoundTag)
fun TagCompound.getByteArrayOrNull(key: String): ByteArray?  = ifPresent(key, NBT.TAG_BYTE_ARRAY, ::getByteArray)
fun TagCompound.getIntArrayOrNull(key: String): IntArray?    = ifPresent(key, NBT.TAG_INT_ARRAY, ::getIntArray)
fun TagCompound.getLongArrayOrNull(key: String): LongArray?  = ifPresent(key, NBT.TAG_LONG_ARRAY, ::getLongArray)

fun TagCompound.getPosOrNull(key: String): BlockPos? = ifPresent(key, NBT.TAG_LONG, ::getPos)

// Lists

fun TagCompound.getListOfPrimitives(key: String): NBTPrimitiveList{
	val tag = this.getTag(key)
	
	return if (tag is TagList && (tag.isEmpty || tag.get(0) is NBTPrimitive))
		NBTPrimitiveList(tag)
	else
		NBTPrimitiveList(TagList())
}

fun TagCompound.getListOfCompounds(key: String)  = NBTObjectList<TagCompound>(this.getTagList(key, NBT.TAG_COMPOUND))
fun TagCompound.getListOfStrings(key: String)    = NBTObjectList<String>(this.getTagList(key, NBT.TAG_STRING))
fun TagCompound.getListOfByteArrays(key: String) = NBTObjectList<ByteArray>(this.getTagList(key, NBT.TAG_BYTE_ARRAY))
fun TagCompound.getListOfIntArrays(key: String)  = NBTObjectList<IntArray>(this.getTagList(key, NBT.TAG_INT_ARRAY))
fun TagCompound.getListOfLongArrays(key: String) = NBTObjectList<LongArray>(this.getTagList(key, NBT.TAG_LONG_ARRAY))

fun TagCompound.getListOfItemStacks(key: String) = NBTItemStackList(this.getTagList(key, NBT.TAG_COMPOUND))

inline fun <reified T : Enum<T>> TagCompound.getListOfEnums(key: String) = NBTEnumList(T::class.java, this.getTagList(key, NBT.TAG_STRING))

abstract class NBTList<T : Any>(protected val tagList: TagList) : Iterable<T>{
	companion object{
		fun <T : Any> TagCompound.setList(key: String, list: NBTList<T>){
			this.setTag(key, list.tagList)
		}
	}
	
	val size
		get() = tagList.tagCount()
	
	val isEmpty
		get() = tagList.isEmpty
	
	protected abstract fun convert(element: T): NBTBase
	
	open fun append(element: T){
		tagList.appendTag(convert(element))
	}
	
	open fun set(index: Int, element: T){
		tagList.set(index, convert(element))
	}
	
	abstract fun get(index: Int) : T
	
	override fun iterator() = object : MutableIterator<T>{
		private var cursor = 0
		private var canRemove = false
		
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
			check(canRemove)
			
			canRemove = false
			tagList.removeTag(--cursor)
		}
	}
	
	override fun equals(other: Any?) = tagList == other
	override fun hashCode() = tagList.hashCode()
	override fun toString() = tagList.toString()
}

class NBTPrimitiveList(tagList: TagList = TagList()) : NBTList<NBTPrimitive>(tagList){
	val allBytes
		get() = this.map(NBTPrimitive::getByte)
	
	val allShorts
		get() = this.map(NBTPrimitive::getShort)
	
	val allInts
		get() = this.map(NBTPrimitive::getInt)
	
	val allLongs
		get() = this.map(NBTPrimitive::getLong)
	
	val allFloats
		get() = this.map(NBTPrimitive::getFloat)
	
	val allDoubles
		get() = this.map(NBTPrimitive::getDouble)
	
	fun append(value: Byte)   = tagList.appendTag(TagByte(value))
	fun append(value: Short)  = tagList.appendTag(TagShort(value))
	fun append(value: Int)    = tagList.appendTag(TagInt(value))
	fun append(value: Long)   = tagList.appendTag(TagLong(value))
	fun append(value: Float)  = tagList.appendTag(TagFloat(value))
	fun append(value: Double) = tagList.appendTag(TagDouble(value))
	
	override fun convert(element: NBTPrimitive) = element
	
	override fun get(index: Int) = when(val tag = tagList.get(index)){
		is NBTPrimitive -> tag
		is TagEnd       -> throw IndexOutOfBoundsException()
		else            -> throw IllegalArgumentException("unhandled NBT type: ${tag::class.java.simpleName}")
	}
}

class NBTObjectList<T : Any>(tagList: TagList = TagList()) : NBTList<T>(tagList){
	override fun convert(element: T) = when(element){
		is TagCompound -> element
		is String      -> TagString(element)
		is ByteArray   -> TagByteArray(element)
		is IntArray    -> TagIntArray(element)
		is LongArray   -> TagLongArray(element)
		else           -> throw IllegalArgumentException("unhandled NBT type conversion: ${element::class.java.simpleName}")
	}
	
	override fun get(index: Int): T{
		val tag = tagList.get(index)
		
		@Suppress("UNCHECKED_CAST")
		return when(tag){
			is TagCompound  -> tag as T
			is TagString    -> tag.string as T
			is TagByteArray -> tag.byteArray as T
			is TagIntArray  -> tag.intArray as T
			is TagLongArray -> tag.data as T
			is TagEnd       -> throw IndexOutOfBoundsException()
			else            -> throw IllegalArgumentException("unhandled NBT type conversion: ${tag::class.java.simpleName}")
		}
	}
	
	companion object{
		@JvmName("ofCompounds")  fun of(elements: Iterable<TagCompound>) = NBTObjectList<TagCompound>().apply { elements.forEach(::append) }
		@JvmName("ofStrings")    fun of(elements: Iterable<String>)      = NBTObjectList<String>().apply      { elements.forEach(::append) }
		@JvmName("ofByteArrays") fun of(elements: Iterable<ByteArray>)   = NBTObjectList<ByteArray>().apply   { elements.forEach(::append) }
		@JvmName("ofIntArrays")  fun of(elements: Iterable<IntArray>)    = NBTObjectList<IntArray>().apply    { elements.forEach(::append) }
		@JvmName("ofLongArrays") fun of(elements: Iterable<LongArray>)   = NBTObjectList<LongArray>().apply   { elements.forEach(::append) }
	}
}

class NBTItemStackList(tagList: TagList = TagList()) : NBTList<ItemStack>(tagList){
	override fun convert(element: ItemStack): NBTBase{
		return TagCompound().also { it.writeStack(element) }
	}
	
	override fun get(index: Int): ItemStack{
		return tagList.getCompoundTagAt(index).readStack()
	}
	
	companion object{
		fun of(elements: Iterable<ItemStack>) = NBTItemStackList().apply { elements.forEach(::append) }
	}
}

class NBTEnumList<T : Enum<T>>(private val cls: Class<T>, tagList: TagList) : NBTList<T>(tagList){
	private constructor(cls: Class<T>) : this(cls, TagList())
	
	companion object{
		fun <T : Enum<T>> of(cls: Class<T>, elements: Iterable<T>) = NBTEnumList(cls).apply { elements.forEach(::append) }
		inline fun <reified T : Enum<T>> of(elements: Iterable<T>) = of(T::class.java, elements)
	}
	
	override fun convert(element: T): NBTBase{
		return TagString(element.name.toLowerCase(Locale.ROOT))
	}
	
	override fun get(index: Int): T{
		return java.lang.Enum.valueOf(cls, tagList.getStringTagAt(index).toUpperCase(Locale.ROOT))
	}
}
