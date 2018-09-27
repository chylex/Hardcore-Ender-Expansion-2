package chylex.hee.system.util
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTSizeTracker
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.network.ByteBufUtils

// BlockPos

inline fun ByteBuf.writePos(pos: BlockPos){
	this.writeLong(pos.toLong())
}

inline fun ByteBuf.readPos(): BlockPos{
	return Pos(this.readLong())
}

// Vec3d (Full)

inline fun ByteBuf.writeVec(vec: Vec3d){
	this.writeDouble(vec.x)
	this.writeDouble(vec.y)
	this.writeDouble(vec.z)
}

inline fun ByteBuf.readVec(): Vec3d{
	return Vec3d(readDouble(), readDouble(), readDouble())
}

// Vec3d (Compact)

inline fun ByteBuf.writeCompactVec(vec: Vec3d){
	this.writeInt((vec.x * 8.0).floorToInt())
	this.writeInt((vec.y * 8.0).floorToInt())
	this.writeInt((vec.z * 8.0).floorToInt())
}

inline fun ByteBuf.readCompactVec(): Vec3d{
	return Vec3d(readInt() * 0.125, readInt() * 0.125, readInt() * 0.125)
}

// Varints

inline fun ByteBuf.writeVarInt(value: Int, maxBytes: Int = 5){
	ByteBufUtils.writeVarInt(this, value, maxBytes)
}

inline fun ByteBuf.readVarInt(maxBytes: Int = 5): Int{
	return ByteBufUtils.readVarInt(this, maxBytes)
}

// NBT

inline fun ByteBuf.writeTag(tag: NBTTagCompound){
	CompressedStreamTools.write(tag, ByteBufOutputStream(this))
}

inline fun ByteBuf.readTag(): NBTTagCompound{
	return CompressedStreamTools.read(ByteBufInputStream(this), NBTSizeTracker(2097152L))
}

// ItemStack

inline fun ByteBuf.writeStack(stack: ItemStack){
	ByteBufUtils.writeItemStack(this, stack)
}

inline fun ByteBuf.readStack(): ItemStack{
	return ByteBufUtils.readItemStack(this)
}

// Arrays (Bytes)

inline fun ByteBuf.writeByteArray(array: ByteArray){
	array.forEach { writeByte(it.toInt()) }
}

inline fun ByteBuf.readByteArray(count: Int): ByteArray{
	return ByteArray(count){ readByte() }
}

// Arrays (Shorts)

inline fun ByteBuf.writeShortArray(array: ShortArray){
	array.forEach { writeShort(it.toInt()) }
}

inline fun ByteBuf.readShortArray(count: Int): ShortArray{
	return ShortArray(count){ readShort() }
}

// Arrays (Ints)

inline fun ByteBuf.writeIntArray(array: IntArray){
	array.forEach { writeInt(it) }
}

inline fun ByteBuf.readIntArray(count: Int): IntArray{
	return IntArray(count){ readInt() }
}

// Arrays (Longs)

inline fun ByteBuf.writeLongArray(array: LongArray){
	array.forEach { writeLong(it) }
}

inline fun ByteBuf.readLongArray(count: Int): LongArray{
	return LongArray(count){ readLong() }
}

// Arrays (Floats)

inline fun ByteBuf.writeFloatArray(array: FloatArray){
	array.forEach { writeFloat(it) }
}

inline fun ByteBuf.readFloatArray(count: Int): FloatArray{
	return FloatArray(count){ readFloat() }
}

// Arrays (Doubles)

inline fun ByteBuf.writeDoubleArray(array: DoubleArray){
	array.forEach { writeDouble(it) }
}

inline fun ByteBuf.readDoubleArray(count: Int): DoubleArray{
	return DoubleArray(count){ readDouble() }
}
