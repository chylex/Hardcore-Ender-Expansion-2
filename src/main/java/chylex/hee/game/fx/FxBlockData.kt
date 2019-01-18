package chylex.hee.game.fx
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.util.math.BlockPos

class FxBlockData(private val pos: BlockPos) : IFxData{
	override fun write(buffer: ByteBuf) = buffer.use {
		writePos(pos)
	}
}
