@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.util.nbt

import chylex.hee.game.inventory.util.nonEmptySlots
import chylex.hee.game.inventory.util.setStack
import chylex.hee.util.math.Pos
import com.mojang.serialization.Codec
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTDynamicOps
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.Constants.NBT
import org.apache.logging.log4j.Logger
import java.util.Locale
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
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

// Presence checks

inline fun TagCompound.hasKey(key: String): Boolean {
	return this.contains(key)
}

inline fun TagCompound.hasKey(key: String, type: Int): Boolean {
	return this.contains(key, type)
}

@OptIn(ExperimentalContracts::class)
@JvmName("isNotNullAndHasKey")
inline fun TagCompound?.hasKey(key: String): Boolean {
	contract { returns(true) implies (this@hasKey != null) }
	return this != null && this.hasKey(key)
}

@OptIn(ExperimentalContracts::class)
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

// Codecs

fun <T> TagCompound.putEncoded(key: String, value: T, codec: Codec<T>, log: Logger) {
	codec.encodeStart(NBTDynamicOps.INSTANCE, value).resultOrPartial(log::error).ifPresent {
		this@putEncoded.put(key, it)
	}
}

fun <T> TagCompound.getDecodedOrNull(key: String, codec: Codec<T>, log: Logger): T? {
	return this.get(key)?.let {
		codec.parse(NBTDynamicOps.INSTANCE, it).resultOrPartial(log::error).orElse(null)
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

// AABB

fun TagCompound.putAABB(key: String, aabb: AxisAlignedBB) {
	this.put(key, TagCompound().apply {
		putDouble("x1", aabb.minX)
		putDouble("x2", aabb.maxX)
		putDouble("y1", aabb.minY)
		putDouble("y2", aabb.maxY)
		putDouble("z1", aabb.minZ)
		putDouble("z2", aabb.maxZ)
	})
}

fun TagCompound.getAABBOrNull(key: String): AxisAlignedBB? {
	return this.getCompoundOrNull(key)?.let {
		AxisAlignedBB(
			getDouble("x1"),
			getDouble("x2"),
			getDouble("y1"),
			getDouble("y2"),
			getDouble("z1"),
			getDouble("z2")
		)
	}
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
	this.putString(key, value?.name?.lowercase(Locale.ROOT) ?: "")
}

inline fun <reified T : Enum<T>> TagCompound.getEnum(key: String): T? {
	val value = this.getString(key)
	
	if (value.isEmpty()) {
		return null
	}
	
	return try {
		java.lang.Enum.valueOf(T::class.java, value.uppercase(Locale.ROOT))
	} catch (e: IllegalArgumentException) {
		null
	}
}

// ItemStacks

fun TagCompound.writeStack(stack: ItemStack) {
	if (!stack.isEmpty) {
		stack.write(this)
	}
}

fun TagCompound.readStack(): ItemStack {
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
	
	for ((slot, stack) in inventory.nonEmptySlots) {
		list.add(TagCompound().also {
			stack.write(it)
			it.putInt(SLOT_TAG, slot)
		})
	}
	
	this.put(key, list)
}

fun TagCompound.loadInventory(key: String, inventory: IInventory) {
	inventory.clear()
	
	for (tag in this.getListOfCompounds(key)) {
		inventory.setStack(tag.getInt(SLOT_TAG), ItemStack.read(tag))
	}
}
