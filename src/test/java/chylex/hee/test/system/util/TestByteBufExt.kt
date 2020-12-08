package chylex.hee.test.system.util
import chylex.hee.game.world.Pos
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.readCompactVec
import chylex.hee.system.serialization.readPos
import chylex.hee.system.serialization.readTag
import chylex.hee.system.serialization.readVec
import chylex.hee.system.serialization.writeCompactVec
import chylex.hee.system.serialization.writePos
import chylex.hee.system.serialization.writeTag
import chylex.hee.system.serialization.writeVec
import io.netty.buffer.Unpooled
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
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
		
		@Test fun `writing and reading full 'Vector3d' objects works`() = with(PacketBuffer(Unpooled.buffer())){
			arrayOf(
				Vector3d.ZERO,
				Vector3d(-1.23, 45.6, -789.0),
				Vector3d(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE),
				Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
			).forEach {
				writeVec(it)
				assertEquals(it, readVec())
			}
		}
		
		@Test fun `writing and reading compact 'Vector3d' objects works`() = with(PacketBuffer(Unpooled.buffer())){
			mapOf(
				Vector3d.ZERO to Vector3d.ZERO,
				Vector3d(-1.23, 45.6, -789.0) to Vector3d(-1.25, 45.5, -789.0)
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
