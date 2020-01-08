package chylex.hee.game.fx
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos

class FxBlockData(private val pos: BlockPos) : IFxData{
	override fun write(buffer: PacketBuffer) = buffer.use {
		writePos(pos)
	}
}
