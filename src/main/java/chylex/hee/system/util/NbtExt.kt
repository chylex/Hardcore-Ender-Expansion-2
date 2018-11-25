package chylex.hee.system.util
import chylex.hee.HEE
import net.minecraft.inventory.IInventory
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
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.Constants.NBT
import org.apache.commons.lang3.ArrayUtils.EMPTY_LONG_ARRAY
import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private const val HEE_TAG_NAME = HEE.ID

val NBTTagCompound.heeTag: NBTTagCompound
	get(){
		return if (this.hasKey(HEE_TAG_NAME))
			this.getCompoundTag(HEE_TAG_NAME)
		else
			NBTTagCompound().also { setTag(HEE_TAG_NAME, it) }
	}

val NBTTagCompound.heeTagOrNull: NBTTagCompound?
	get(){
		return if (this.hasKey(HEE_TAG_NAME))
			this.getCompoundTag(HEE_TAG_NAME)
		else
			null
	}

// ItemStack NBT

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

/**
 * Returns the ItemStack's HEE tag from its main NBT tag. If the ItemStack has neither the main NBT tag nor the HEE tag, they will be created.
 */
val ItemStack.heeTag: NBTTagCompound
	get() = this.nbt.heeTag

/**
 * Returns the ItemStack's HEE tag from its main NBT tag. If the ItemStack has neither the main NBT tag nor the HEE tag, null is returned instead.
 */
val ItemStack.heeTagOrNull: NBTTagCompound?
	get() = this.nbtOrNull?.heeTagOrNull

/**
 * Recursively deletes all empty compound tags in the ItemStack.
 */
fun ItemStack.cleanupNBT(){
	fun cleanupTag(tag: NBTTagCompound){
		tag.keySet.removeIf {
			val nested = tag.getTag(it) as? NBTTagCompound
			nested != null && nested.apply(::cleanupTag).isEmpty
		}
	}
	
	val nbt = this.nbtOrNull
	
	if (nbt != null && nbt.apply(::cleanupTag).isEmpty){
		this.tagCompound = null
	}
}

// ItemStacks

inline fun NBTTagCompound.writeStack(stack: ItemStack){
	if (stack.isNotEmpty){
		stack.writeToNBT(this)
	}
}

inline fun NBTTagCompound.readStack(): ItemStack{
	return if (this.size == 0)
		ItemStack.EMPTY
	else
		ItemStack(this)
}

fun NBTTagCompound.setStack(key: String, stack: ItemStack){
	this.setTag(key, NBTTagCompound().also { it.writeStack(stack) })
}

fun NBTTagCompound.getStack(key: String): ItemStack{
	return this.getCompoundTag(key).readStack()
}

// Inventories

fun NBTTagCompound.saveInventory(key: String, inventory: IInventory){
	val list = NBTTagList()
	
	for((slot, stack) in inventory.nonEmptySlots){
		list.appendTag(NBTTagCompound().also {
			stack.writeToNBT(it)
			it.setInteger("Slot", slot)
		})
	}
	
	this.setTag(key, list)
}

fun NBTTagCompound.loadInventory(key: String, inventory: IInventory){
	inventory.clear()
	
	for(tag in this.getListOfCompounds(key)){
		inventory.setStack(tag.getInteger("Slot"), ItemStack(tag))
	}
}

// Long arrays

fun NBTTagCompound.setLongArray(key: String, array: LongArray){
	this.setTag(key, NBTTagLongArray(array))
}

fun NBTTagCompound.getLongArray(key: String): LongArray{
	return if (this.hasKey(key, NBT.TAG_LONG_ARRAY))
		(this.getTag(key) as? NBTTagLongArray)?.data ?: EMPTY_LONG_ARRAY
	else
		EMPTY_LONG_ARRAY
}

// BlockPos

inline fun NBTTagCompound.setPos(key: String, pos: BlockPos){
	this.setLong(key, pos.toLong())
}

inline fun NBTTagCompound.getPos(key: String): BlockPos{
	return if (this.hasKey(key, NBT.TAG_LONG))
		Pos(this.getLong(key))
	else
		BlockPos.ORIGIN
}

// Enums

inline fun <reified T : Enum<T>> NBTTagCompound.setEnum(key: String, value: T?){
	this.setString(key, value?.name?.toLowerCase(Locale.ROOT) ?: "")
}

inline fun <reified T : Enum<T>> NBTTagCompound.getEnum(key: String): T?{
	val value = this.getString(key)
	
	return if (value.isEmpty())
		null
	else
		java.lang.Enum.valueOf(T::class.java, value.toUpperCase(Locale.ROOT))
}

// Presence checks

@UseExperimental(ExperimentalContracts::class)
inline fun NBTTagCompound?.hasKey(key: String): Boolean{
	contract { returns(true) implies (this@hasKey != null) }
	return this != null && this.hasKey(key)
}

@UseExperimental(ExperimentalContracts::class)
inline fun NBTTagCompound?.hasKey(key: String, type: Int): Boolean{
	contract { returns(true) implies (this@hasKey != null) }
	return this != null && this.hasKey(key, type)
}

inline fun <T> NBTTagCompound?.ifPresent(key: String, type: Int, getter: (String) -> T): T?{
	return if (this.hasKey(key, type))
		getter(key)
	else
		null
}

fun NBTTagCompound.getByteOrNull(key: String): Byte?               = ifPresent(key, NBT.TAG_BYTE, ::getByte)
fun NBTTagCompound.getShortOrNull(key: String): Short?             = ifPresent(key, NBT.TAG_SHORT, ::getShort)
fun NBTTagCompound.getIntegerOrNull(key: String): Int?             = ifPresent(key, NBT.TAG_INT, ::getInteger)
fun NBTTagCompound.getLongOrNull(key: String): Long?               = ifPresent(key, NBT.TAG_LONG, ::getLong)
fun NBTTagCompound.getFloatOrNull(key: String): Float?             = ifPresent(key, NBT.TAG_FLOAT, ::getFloat)
fun NBTTagCompound.getDoubleOrNull(key: String): Double?           = ifPresent(key, NBT.TAG_DOUBLE, ::getDouble)
fun NBTTagCompound.getStringOrNull(key: String): String?           = ifPresent(key, NBT.TAG_STRING, ::getString)
fun NBTTagCompound.getCompoundOrNull(key: String): NBTTagCompound? = ifPresent(key, NBT.TAG_COMPOUND, ::getCompoundTag)
fun NBTTagCompound.getByteArrayOrNull(key: String): ByteArray?     = ifPresent(key, NBT.TAG_BYTE_ARRAY, ::getByteArray)
fun NBTTagCompound.getIntArrayOrNull(key: String): IntArray?       = ifPresent(key, NBT.TAG_INT_ARRAY, ::getIntArray)
fun NBTTagCompound.getLongArrayOrNull(key: String): LongArray?     = ifPresent(key, NBT.TAG_LONG_ARRAY, ::getLongArray)

fun NBTTagCompound.getPosOrNull(key: String): BlockPos? = ifPresent(key, NBT.TAG_LONG, ::getPos)

// Lists

fun NBTTagCompound.getListOfPrimitives(key: String): NBTPrimitiveList{
	val tag = this.getTag(key)
	
	return if (tag is NBTTagList && (tag.isEmpty || tag.get(0) is NBTPrimitive))
		NBTPrimitiveList(tag)
	else
		NBTPrimitiveList(NBTTagList())
}

fun NBTTagCompound.getListOfCompounds(key: String)  = NBTObjectList<NBTTagCompound>(this.getTagList(key, NBT.TAG_COMPOUND))
fun NBTTagCompound.getListOfStrings(key: String)    = NBTObjectList<String>(this.getTagList(key, NBT.TAG_STRING))
fun NBTTagCompound.getListOfByteArrays(key: String) = NBTObjectList<ByteArray>(this.getTagList(key, NBT.TAG_BYTE_ARRAY))
fun NBTTagCompound.getListOfIntArrays(key: String)  = NBTObjectList<IntArray>(this.getTagList(key, NBT.TAG_INT_ARRAY))
fun NBTTagCompound.getListOfLongArrays(key: String) = NBTObjectList<LongArray>(this.getTagList(key, NBT.TAG_LONG_ARRAY))

fun NBTTagCompound.getListOfItemStacks(key: String) = NBTItemStackList(this.getTagList(key, NBT.TAG_COMPOUND))

inline fun <reified T : Enum<T>> NBTTagCompound.getListOfEnums(key: String) = NBTEnumList(T::class.java, this.getTagList(key, NBT.TAG_STRING))

abstract class NBTList<T : Any>(protected val tagList: NBTTagList) : Iterable<T>{
	companion object{
		fun <T : Any> NBTTagCompound.setList(key: String, list: NBTList<T>){
			this.setTag(key, list.tagList)
		}
	}
	
	val size
		get() = tagList.tagCount()
	
	val isEmpty
		get() = tagList.isEmpty
	
	abstract fun append(element: T)
	abstract fun get(index: Int) : T
	
	override fun iterator(): MutableIterator<T> = object : MutableIterator<T>{
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

class NBTPrimitiveList(tagList: NBTTagList = NBTTagList()) : NBTList<NBTPrimitive>(tagList){
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

class NBTObjectList<T : Any>(tagList: NBTTagList = NBTTagList()) : NBTList<T>(tagList){
	override fun append(element: T){
		tagList.appendTag(when(element){
			is NBTTagCompound -> element
			is String         -> NBTTagString(element)
			is ByteArray      -> NBTTagByteArray(element)
			is IntArray       -> NBTTagIntArray(element)
			is LongArray      -> NBTTagLongArray(element)
			else              -> throw IllegalArgumentException("unhandled NBT type conversion: ${element::class.java.simpleName}")
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
			is NBTTagLongArray -> tag.data as T
			is NBTTagEnd       -> throw IndexOutOfBoundsException()
			else               -> throw IllegalArgumentException("unhandled NBT type conversion: ${tag::class.java.simpleName}")
		}
	}
	
	companion object{
		@JvmName("ofCompounds")  fun of(elements: Iterable<NBTTagCompound>) = NBTObjectList<NBTTagCompound>().apply { elements.forEach(::append) }
		@JvmName("ofStrings")    fun of(elements: Iterable<String>)         = NBTObjectList<String>().apply         { elements.forEach(::append) }
		@JvmName("ofByteArrays") fun of(elements: Iterable<ByteArray>)      = NBTObjectList<ByteArray>().apply      { elements.forEach(::append) }
		@JvmName("ofIntArrays")  fun of(elements: Iterable<IntArray>)       = NBTObjectList<IntArray>().apply       { elements.forEach(::append) }
		@JvmName("ofLongArrays") fun of(elements: Iterable<LongArray>)      = NBTObjectList<LongArray>().apply      { elements.forEach(::append) }
	}
}

class NBTItemStackList(tagList: NBTTagList = NBTTagList()) : NBTList<ItemStack>(tagList){
	override fun append(element: ItemStack){
		tagList.appendTag(NBTTagCompound().also { it.writeStack(element) })
	}
	
	override fun get(index: Int): ItemStack{
		return tagList.getCompoundTagAt(index).readStack()
	}
	
	companion object{
		fun of(elements: Iterable<ItemStack>) = NBTItemStackList().apply { elements.forEach(::append) }
	}
}

class NBTEnumList<T : Enum<T>>(private val cls: Class<T>, tagList: NBTTagList) : NBTList<T>(tagList){
	private constructor(cls: Class<T>) : this(cls, NBTTagList())
	
	companion object{
		fun <T : Enum<T>> of(cls: Class<T>, elements: Iterable<T>) = NBTEnumList(cls).apply { elements.forEach(::append) }
		inline fun <reified T : Enum<T>> of(elements: Iterable<T>) = of(T::class.java, elements)
	}
	
	override fun append(element: T){
		tagList.appendTag(NBTTagString(element.name.toLowerCase(Locale.ROOT)))
	}
	
	override fun get(index: Int): T{
		return java.lang.Enum.valueOf(cls, tagList.getStringTagAt(index).toUpperCase(Locale.ROOT))
	}
}
