package chylex.hee.game.fx

import chylex.hee.util.buffer.writePos
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos

class FxBlockData(private val pos: BlockPos) : IFxData {
	override fun write(buffer: PacketBuffer) {
		buffer.writePos(pos)
	}
}
