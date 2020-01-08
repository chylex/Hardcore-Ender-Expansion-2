package chylex.hee.test.system.util
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.readCompactVec
import chylex.hee.system.util.readPos
import chylex.hee.system.util.readTag
import chylex.hee.system.util.readVec
import chylex.hee.system.util.writeCompactVec
import chylex.hee.system.util.writePos
import chylex.hee.system.util.writeTag
import chylex.hee.system.util.writeVec
import io.netty.buffer.Unpooled
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestByteBufExt{
	init{
		Bootstrap.register()
	}
	
	@Nested inner class Basic{
		@Test fun `writing and reading 'Pos' objects works`() = with(PacketBuffer(Unpooled.buffer())){
			arrayOf(
				BlockPos.ZERO,
				Pos(1, 20, -300),
				Pos(-30000000, 0, 30000000),
				Pos(30000000, 2047, -30000000)
			).forEach {
				writePos(it)
				assertEquals(it, readPos())
			}
		}
		
		@Test fun `writing and reading full 'Vec3d' objects works`() = with(PacketBuffer(Unpooled.buffer())){
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
		
		@Test fun `writing and reading compact 'Vec3d' objects works`() = with(PacketBuffer(Unpooled.buffer())){
			mapOf(
				Vec3d.ZERO to Vec3d.ZERO,
				Vec3d(-1.23, 45.6, -789.0) to Vec3d(-1.25, 45.5, -789.0)
			).forEach {
				writeCompactVec(it.key)
				assertEquals(it.value, readCompactVec())
			}
		}
	}
	
	@Nested inner class Complex{
		@Test fun `writing and reading 'TagCompound' works`() = with(PacketBuffer(Unpooled.buffer())){
			arrayOf(
				TagCompound(),
				TagCompound().apply { putLong("test", 123L) },
				TagCompound().apply { TagCompound().apply { putString("key", "hello world") } }
			).forEach {
				writeTag(it)
				assertEquals(it, readTag())
			}
		}
	}
}
