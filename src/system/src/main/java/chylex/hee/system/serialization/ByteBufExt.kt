@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.serialization

import chylex.hee.HEE
import chylex.hee.game.world.Pos
import chylex.hee.system.math.Vec
import chylex.hee.system.math.floorToInt
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

inline fun <T : PacketBuffer> T.use(block: T.() -> Unit) {
	block()
}

// BlockPos

inline fun PacketBuffer.writePos(pos: BlockPos) {
	this.writeLong(pos.toLong())
}

inline fun PacketBuffer.readPos(): BlockPos {
	return Pos(this.readLong())
}

// Vec3d (Full)

fun PacketBuffer.writeVec(vec: Vector3d) {
	this.writeDouble(vec.x)
	this.writeDouble(vec.y)
	this.writeDouble(vec.z)
}

fun PacketBuffer.readVec(): Vector3d {
	return Vec(readDouble(), readDouble(), readDouble())
}

// Vec3d (Float)

fun PacketBuffer.writeFloatVec(vec: Vector3d) {
	this.writeFloat(vec.x.toFloat())
	this.writeFloat(vec.y.toFloat())
	this.writeFloat(vec.z.toFloat())
}

fun PacketBuffer.readFloatVec(): Vector3d {
	return Vec(readFloat().toDouble(), readFloat().toDouble(), readFloat().toDouble())
}

// Vec3d (Compact)

fun PacketBuffer.writeCompactVec(vec: Vector3d) {
	this.writeInt((vec.x * 8.0).floorToInt())
	this.writeInt((vec.y * 8.0).floorToInt())
	this.writeInt((vec.z * 8.0).floorToInt())
}

fun PacketBuffer.readCompactVec(): Vector3d {
	return Vec(readInt() * 0.125, readInt() * 0.125, readInt() * 0.125)
}

// NBT

fun ByteBuf.writeTag(tag: TagCompound) {
	CompressedStreamTools.write(tag, ByteBufOutputStream(this))
}

fun ByteBuf.readTag(): TagCompound {
	return CompressedStreamTools.read(ByteBufInputStream(this), NBTSizeTracker(2097152L))
}

// Codecs

fun <T> ByteBuf.writeEncoded(value: T, codec: Codec<T>) {
	val result = codec.encodeStart(NBTDynamicOps.INSTANCE, value).resultOrPartial(HEE.log::error)
	if (result.isPresent) {
		this.writeBoolean(true)
		this.writeTag(TagCompound().apply { put("", result.get()) })
	}
	else {
		this.writeBoolean(false)
	}
}

fun <T> ByteBuf.readDecoded(codec: Codec<T>): T? {
	return if (this.readBoolean())
		codec.parse(NBTDynamicOps.INSTANCE, this.readTag().get("")).resultOrPartial(HEE.log::error).orElse(null)
	else
		null
}
