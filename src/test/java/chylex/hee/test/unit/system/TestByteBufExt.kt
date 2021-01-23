package chylex.hee.test.unit.system

import chylex.hee.game.world.Pos
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
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
import net.minecraft.util.registry.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestByteBufExt {
	init {
		Bootstrap.register()
	}
	
	@Nested inner class Basic {
		@Test fun `writing and reading 'Pos' objects works`() = with(PacketBuffer(Unpooled.buffer())) {
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
		
		@Test fun `writing and reading full 'Vec3d' objects works`() = with(PacketBuffer(Unpooled.buffer())) {
			arrayOf(
				Vec3.ZERO,
				Vec(-1.23, 45.6, -789.0),
				Vec3.xyz(Double.MIN_VALUE),
				Vec3.xyz(Double.MAX_VALUE)
			).forEach {
				writeVec(it)
				assertEquals(it, readVec())
			}
		}
		
		@Test fun `writing and reading compact 'Vec3d' objects works`() = with(PacketBuffer(Unpooled.buffer())) {
			mapOf(
				Vec3.ZERO to Vec3.ZERO,
				Vec(-1.23, 45.6, -789.0) to Vec(-1.25, 45.5, -789.0)
			).forEach {
				writeCompactVec(it.key)
				assertEquals(it.value, readCompactVec())
			}
		}
	}
	
	@Nested inner class Complex {
		@Test fun `writing and reading 'TagCompound' works`() = with(PacketBuffer(Unpooled.buffer())) {
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
