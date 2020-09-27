package chylex.hee.network.fx
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writePos
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos

class FxBlockData(private val pos: BlockPos) : IFxData{
	override fun write(buffer: PacketBuffer) = buffer.use {
		writePos(pos)
	}
}
