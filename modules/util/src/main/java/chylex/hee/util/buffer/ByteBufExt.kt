@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.util.buffer

import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec
import chylex.hee.util.math.floorToInt
import chylex.hee.util.nbt.TagCompound
import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTDynamicOps
import net.minecraft.nbt.NBTSizeTracker
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import org.apache.logging.log4j.Logger

// BlockPos

inline fun ByteBuf.writePos(pos: BlockPos) {
	this.writeLong(pos.toLong())
}

inline fun ByteBuf.readPos(): BlockPos {
	return Pos(this.readLong())
}

// Vec3d (Full)

fun ByteBuf.writeVec(vec: Vector3d) {
	this.writeDouble(vec.x)
	this.writeDouble(vec.y)
	this.writeDouble(vec.z)
}

fun ByteBuf.readVec(): Vector3d {
	return Vec(this.readDouble(), this.readDouble(), this.readDouble())
}

// Vec3d (Float)

fun ByteBuf.writeFloatVec(vec: Vector3d) {
	this.writeFloat(vec.x.toFloat())
	this.writeFloat(vec.y.toFloat())
	this.writeFloat(vec.z.toFloat())
}

fun ByteBuf.readFloatVec(): Vector3d {
	return Vec(this.readFloat().toDouble(), this.readFloat().toDouble(), this.readFloat().toDouble())
}

// Vec3d (Compact)

fun ByteBuf.writeCompactVec(vec: Vector3d) {
	this.writeInt((vec.x * 8.0).floorToInt())
	this.writeInt((vec.y * 8.0).floorToInt())
	this.writeInt((vec.z * 8.0).floorToInt())
}

fun ByteBuf.readCompactVec(): Vector3d {
	return Vec(this.readInt() * 0.125, this.readInt() * 0.125, this.readInt() * 0.125)
}

// Enum

fun <T : Enum<T>> PacketBuffer.writeEnum(value: T?) {
	this.writeVarInt(value?.ordinal ?: -1)
}

inline fun <reified T : Enum<T>> PacketBuffer.readEnum(): T? {
	val ordinal = this.readVarInt()
	
	return if (ordinal >= 0)
		T::class.java.enumConstants.getOrNull(ordinal)
	else
		null
}

// NBT

fun ByteBuf.writeTag(tag: TagCompound) {
	CompressedStreamTools.write(tag, ByteBufOutputStream(this))
}

fun ByteBuf.readTag(): TagCompound {
	return CompressedStreamTools.read(ByteBufInputStream(this), NBTSizeTracker(2097152L))
}

// Codecs

fun <T> ByteBuf.writeEncoded(value: T, codec: Codec<T>, log: Logger) {
	val result = codec.encodeStart(NBTDynamicOps.INSTANCE, value).resultOrPartial(log::error)
	if (result.isPresent) {
		this.writeBoolean(true)
		this.writeTag(TagCompound().apply { put("", result.get()) })
	}
	else {
		this.writeBoolean(false)
	}
}

fun <T> ByteBuf.readDecoded(codec: Codec<T>, log: Logger): T? {
	return if (this.readBoolean())
		codec.parse(NBTDynamicOps.INSTANCE, this.readTag().get("")).resultOrPartial(log::error).orElse(null)
	else
		null
}
