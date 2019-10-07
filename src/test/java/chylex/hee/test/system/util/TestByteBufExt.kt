package chylex.hee.test.system.util
import chylex.hee.system.util.Pos
import chylex.hee.system.util.readByteArray
import chylex.hee.system.util.readCompactVec
import chylex.hee.system.util.readDoubleArray
import chylex.hee.system.util.readFloatArray
import chylex.hee.system.util.readIntArray
import chylex.hee.system.util.readLongArray
import chylex.hee.system.util.readPos
import chylex.hee.system.util.readShortArray
import chylex.hee.system.util.readStack
import chylex.hee.system.util.readString
import chylex.hee.system.util.readTag
import chylex.hee.system.util.readVarInt
import chylex.hee.system.util.readVec
import chylex.hee.system.util.writeByteArray
import chylex.hee.system.util.writeCompactVec
import chylex.hee.system.util.writeDoubleArray
import chylex.hee.system.util.writeFloatArray
import chylex.hee.system.util.writeIntArray
import chylex.hee.system.util.writeLongArray
import chylex.hee.system.util.writePos
import chylex.hee.system.util.writeShortArray
import chylex.hee.system.util.writeStack
import chylex.hee.system.util.writeString
import chylex.hee.system.util.writeTag
import chylex.hee.system.util.writeVarInt
import chylex.hee.system.util.writeVec
import io.netty.buffer.Unpooled
import net.minecraft.init.Bootstrap
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import chylex.hee.system.util.TagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestByteBufExt{
	init{
		Bootstrap.register()
	}
	
	@Nested inner class Basic{
		@Test fun `writing and reading 'Pos' objects works`() = with(Unpooled.buffer()){
			arrayOf(
				BlockPos.ORIGIN,
				Pos(1, 20, -300),
				Pos(-30000000, 0, 30000000),
				Pos(30000000, 2047, -30000000)
			).forEach {
				writePos(it)
				assertEquals(it, readPos())
			}
		}
		
		@Test fun `writing and reading full 'Vec3d' objects works`() = with(Unpooled.buffer()){
			arrayOf(
				Vec3d.ZERO,
				Vec3d(-1.23, 45.6, -789.0),
				Vec3d(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE),
				Vec3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
			).forEach {
				writeVec(it)
				assertEquals(it, readVec())
			}
		}
		
		@Test fun `writing and reading compact 'Vec3d' objects works`() = with(Unpooled.buffer()){
			mapOf(
				Vec3d.ZERO to Vec3d.ZERO,
				Vec3d(-1.23, 45.6, -789.0) to Vec3d(-1.25, 45.5, -789.0)
			).forEach {
				writeCompactVec(it.key)
				assertEquals(it.value, readCompactVec())
			}
		}
		
		@Test fun `writing and reading varints works`() = with(Unpooled.buffer()){
			arrayOf(
				0,
				127,
				255,
				32767,
				16777215,
				2147483647
			).forEach {
				writeVarInt(it)
				assertEquals(it, readVarInt())
			}
		}
		
		@Test fun `writing and reading 'String' works`() = with(Unpooled.buffer()){
			arrayOf(
				"",
				"hello world!",
				"ᐊᓕᒍᖅ ᓂᕆᔭᕌᖓᒃᑯ ᓱᕋᙱᑦᑐᓐᓇᖅᑐᖓ"
			).forEach {
				writeString(it)
				assertEquals(it, readString())
			}
		}
	}
	
	@Nested inner class Complex{
		@Test fun `writing and reading 'TagCompound' works`() = with(Unpooled.buffer()){
			arrayOf(
				TagCompound(),
				TagCompound().apply { setLong("test", 123L) },
				TagCompound().apply { TagCompound().apply { setString("key", "hello world") } }
			).forEach {
				writeTag(it)
				assertEquals(it, readTag())
			}
		}
		
		@Test fun `writing and reading 'ItemStack' works`() = with(Unpooled.buffer()){
			arrayOf(
				ItemStack.EMPTY,
				ItemStack(Items.BOW),
				ItemStack(Items.ARROW, 32).apply { setStackDisplayName("Test") }
			).forEach {
				writeStack(it)
				assertTrue(ItemStack.areItemStacksEqual(it, readStack()))
			}
		}
	}
	
	@Nested inner class Arrays{
		@Test fun `writing and reading a byte array works`() = with(Unpooled.buffer()){
			arrayOf(
				byteArrayOf(),
				byteArrayOf(0),
				byteArrayOf(Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE)
			).forEach {
				writeByteArray(it)
				assertArrayEquals(it, readByteArray(it.size))
			}
		}
		
		@Test fun `writing and reading a short array works`() = with(Unpooled.buffer()){
			arrayOf(
				shortArrayOf(),
				shortArrayOf(0),
				shortArrayOf(Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE)
			).forEach {
				writeShortArray(it)
				assertArrayEquals(it, readShortArray(it.size))
			}
		}
		
		@Test fun `writing and reading an int array works`() = with(Unpooled.buffer()){
			arrayOf(
				intArrayOf(),
				intArrayOf(0),
				intArrayOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE)
			).forEach {
				writeIntArray(it)
				assertArrayEquals(it, readIntArray(it.size))
			}
		}
		
		@Test fun `writing and reading a long array works`() = with(Unpooled.buffer()){
			arrayOf(
				longArrayOf(),
				longArrayOf(0),
				longArrayOf(Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE)
			).forEach {
				writeLongArray(it)
				assertArrayEquals(it, readLongArray(it.size))
			}
		}
		
		@Test fun `writing and reading a float array works`() = with(Unpooled.buffer()){
			arrayOf(
				floatArrayOf(),
				floatArrayOf(0F),
				floatArrayOf(Float.MIN_VALUE, -2.5F, -1F, 0F, 1F, 2.5F, Float.MAX_VALUE)
			).forEach {
				writeFloatArray(it)
				assertArrayEquals(it, readFloatArray(it.size))
			}
		}
		
		@Test fun `writing and reading a double array works`() = with(Unpooled.buffer()){
			arrayOf(
				doubleArrayOf(),
				doubleArrayOf(0.0),
				doubleArrayOf(Double.MIN_VALUE, -2.5, -1.0, 0.0, 1.0, 2.5, Double.MAX_VALUE)
			).forEach {
				writeDoubleArray(it)
				assertArrayEquals(it, readDoubleArray(it.size))
			}
		}
	}
}
