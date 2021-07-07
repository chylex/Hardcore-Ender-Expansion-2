package chylex.hee.game.fx

import chylex.hee.util.buffer.writeVec
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.vector.Vector3d

class FxVecData(private val vec: Vector3d) : IFxData {
	override fun write(buffer: PacketBuffer) {
		buffer.writeVec(vec)
	}
}
