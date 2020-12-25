@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.serialization

import chylex.hee.HEE
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.inventory.nonEmptySlots
import chylex.hee.game.inventory.setStack
import chylex.hee.game.world.Pos
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.INBT
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.Constants.NBT
import java.util.Locale
import java.util.UUID
import kotlin.contracts.contract

typealias NBTBase      = net.minecraft.nbt.INBT
typealias NBTPrimitive = net.minecraft.nbt.NumberNBT
typealias TagByte      = net.minecraft.nbt.ByteNBT
typealias TagByteArray = net.minecraft.nbt.ByteArrayNBT
typealias TagCompound  = net.minecraft.nbt.CompoundNBT
typealias TagDouble    = net.minecraft.nbt.DoubleNBT
typealias TagEnd       = net.minecraft.nbt.EndNBT
typealias TagFloat     = net.minecraft.nbt.FloatNBT
typealias TagInt       = net.minecraft.nbt.IntNBT
typealias TagIntArray  = net.minecraft.nbt.IntArrayNBT
typealias TagList      = net.minecraft.nbt.ListNBT
typealias TagLong      = net.minecraft.nbt.LongNBT
typealias TagLongArray = net.minecraft.nbt.LongArrayNBT
typealias TagShort     = net.minecraft.nbt.ShortNBT
typealias TagString    = net.minecraft.nbt.StringNBT

fun TagCompound.getOrCreateCompound(key: String): TagCompound {
	return if (this.hasKey(key))
		this.getCompound(key)
	else
		TagCompound().also { put(key, it) }
}

inline fun TagCompound.use(block: TagCompound.() -> Unit) {
	block()
}

// HEE tag

private const val HEE_TAG_NAME = HEE.ID

val TagCompound.heeTag
	get() = this.getOrCreateCompound(HEE_TAG_NAME)

val TagCompound.heeTagOrNull
	get() = this.getCompoundOrNull(HEE_TAG_NAME)

// ItemStacks

inline fun TagCompound.writeStack(stack: ItemStack) {
	if (stack.isNotEmpty) {
		stack.write(this)
	}
}

inline fun TagCompound.readStack(): ItemStack {
	return if (this.size() == 0)
		ItemStack.EMPTY
	else
		ItemStack.read(this)
}

fun TagCompound.putStack(key: String, stack: ItemStack) {
	this.put(key, TagCompound().also { it.writeStack(stack) })
}

fun TagCompound.getStack(key: String): ItemStack {
	return this.getCompound(key).readStack()
}

// Inventories

private const val SLOT_TAG = "Slot"

fun TagCompound.hasInventory(key: String): Boolean {
	return this.contains(key, NBT.TAG_LIST)
}

fun TagCompound.saveInventory(key: String, inventory: IInventory) {
	val list = TagList()
	
	for((slot, stack) in inventory.nonEmptySlots) {
		list.add(TagCompound().also {
			stack.write(it)
			it.putInt(SLOT_TAG, slot)
		})
	}
	
	this.put(key, list)
}

fun TagCompound.loadInventory(key: String, inventory: IInventory) {
	inventory.clear()
	
	for(tag in this.getListOfCompounds(key)) {
		inventory.setStack(tag.getInt(SLOT_TAG), ItemStack.read(tag))
	}
}

// BlockPos

inline fun TagCompound.putPos(key: String, pos: BlockPos) {
	this.putLong(key, pos.toLong())
}

fun TagCompound.getPos(key: String): BlockPos {
	return if (this.hasKey(key, NBT.TAG_LONG))
		Pos(this.getLong(key))
	else
		BlockPos.ZERO
}

// UUID

inline fun TagCompound.hasUUID(key: String): Boolean {
	return this.hasUniqueId(key)
}

inline fun TagCompound.putUUID(key: String, uuid: UUID) {
	this.putUniqueId(key, uuid)
}

inline fun TagCompound.getUUID(key: String): UUID {
	return this.getUniqueId(key)
}

// Enums

inline fun <reified T : Enum<T>> TagCompound.putEnum(key: String, value: T?) {
	this.putString(key, value?.name?.toLowerCase(Locale.ROOT) ?: "")
}

inline fun <reified T : Enum<T>> TagCompound.getEnum(key: String): T? {
	val value = this.getString(key)
	
	if (value.isEmpty()) {
		return null
	}
	
	return try {
		java.lang.Enum.valueOf(T::class.java, value.toUpperCase(Locale.ROOT))
	} catch(e: IllegalArgumentException) {
		null
	}
}

// Presence checks

inline fun TagCompound.hasKey(key: String): Boolean {
	return this.contains(key)
}

inline fun TagCompound.hasKey(key: String, type: Int): Boolean {
	return this.contains(key, type)
}

@JvmName("isNotNullAndHasKey")
inline fun TagCompound?.hasKey(key: String): Boolean {
	contract { returns(true) implies (this@hasKey != null) }
	return this != null && this.hasKey(key)
}

@JvmName("isNotNullAndHasKey")
inline fun TagCompound?.hasKey(key: String, type: Int): Boolean {
	contract { returns(true) implies (this@hasKey != null) }
	return this != null && this.hasKey(key, type)
}

inline fun <T> TagCompound?.ifPresent(key: String, type: Int, getter: (String) -> T): T? {
	return if (this.hasKey(key, type))
		getter(key)
	else
		null
}

fun TagCompound.getByteOrNull(key: String): Byte?            = ifPresent(key, NBT.TAG_BYTE, ::getByte)
fun TagCompound.getShortOrNull(key: String): Short?          = ifPresent(key, NBT.TAG_SHORT, ::getShort)
fun TagCompound.getIntegerOrNull(key: String): Int?          = ifPresent(key, NBT.TAG_INT, ::getInt)
fun TagCompound.getLongOrNull(key: String): Long?            = ifPresent(key, NBT.TAG_LONG, ::getLong)
fun TagCompound.getFloatOrNull(key: String): Float?          = ifPresent(key, NBT.TAG_FLOAT, ::getFloat)
fun TagCompound.getDoubleOrNull(key: String): Double?        = ifPresent(key, NBT.TAG_DOUBLE, ::getDouble)
fun TagCompound.getStringOrNull(key: String): String?        = ifPresent(key, NBT.TAG_STRING, ::getString)
fun TagCompound.getCompoundOrNull(key: String): TagCompound? = ifPresent(key, NBT.TAG_COMPOUND, ::getCompound)
fun TagCompound.getByteArrayOrNull(key: String): ByteArray?  = ifPresent(key, NBT.TAG_BYTE_ARRAY, ::getByteArray)
fun TagCompound.getIntArrayOrNull(key: String): IntArray?    = ifPresent(key, NBT.TAG_INT_ARRAY, ::getIntArray)
fun TagCompound.getLongArrayOrNull(key: String): LongArray?  = ifPresent(key, NBT.TAG_LONG_ARRAY, ::getLongArray)

fun TagCompound.getPosOrNull(key: String): BlockPos? = ifPresent(key, NBT.TAG_LONG, ::getPos)

// Lists

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

abstract class NBTList<T : Any>(protected val tagList: TagList) : Iterable<T> {
	companion object {
		fun <T : Any> TagCompound.putList(key: String, list: NBTList<T>) {
			this.put(key, list.tagList)
		}
	}
	
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

class NBTPrimitiveList(tagList: TagList = TagList()) : NBTList<NBTPrimitive>(tagList) {
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
	
	fun append(value: Byte)   = tagList.add(TagByte.valueOf(value))
	fun append(value: Short)  = tagList.add(TagShort.valueOf(value))
	fun append(value: Int)    = tagList.add(TagInt.valueOf(value))
	fun append(value: Long)   = tagList.add(TagLong.valueOf(value))
	fun append(value: Float)  = tagList.add(TagFloat.valueOf(value))
	fun append(value: Double) = tagList.add(TagDouble.valueOf(value))
	
	override fun convert(element: NBTPrimitive) = element
	
	override fun get(index: Int) = when(val tag = tagList[index]) {
		is NBTPrimitive -> tag
		is TagEnd       -> throw IndexOutOfBoundsException()
		else            -> throw IllegalArgumentException("unhandled NBT type: ${tag::class.java.simpleName}")
	}
}

class NBTObjectList<T : Any>(tagList: TagList = TagList()) : NBTList<T>(tagList) {
	override fun convert(element: T): INBT = when(element) {
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
		return when(tag) {
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

class NBTItemStackList(tagList: TagList = TagList()) : NBTList<ItemStack>(tagList) {
	override fun convert(element: ItemStack): NBTBase {
		return TagCompound().also { it.writeStack(element) }
	}
	
	override fun get(index: Int): ItemStack {
		return tagList.getCompound(index).readStack()
	}
	
	companion object {
		fun of(elements: Iterable<ItemStack>) = NBTItemStackList().apply { elements.forEach(::append) }
	}
}

class NBTEnumList<T : Enum<T>>(private val cls: Class<T>, tagList: TagList) : NBTList<T>(tagList) {
	private constructor(cls: Class<T>) : this(cls, TagList())
	
	companion object {
		fun <T : Enum<T>> of(cls: Class<T>, elements: Iterable<T>) = NBTEnumList(cls).apply { elements.forEach(::append) }
		inline fun <reified T : Enum<T>> of(elements: Iterable<T>) = of(T::class.java, elements)
	}
	
	override fun convert(element: T): NBTBase {
		return TagString.valueOf(element.name.toLowerCase(Locale.ROOT))
	}
	
	override fun get(index: Int): T {
		return java.lang.Enum.valueOf(cls, tagList.getString(index).toUpperCase(Locale.ROOT))
	}
}
