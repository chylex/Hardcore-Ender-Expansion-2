package chylex.hee.network.fx

import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeVec
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.vector.Vector3d

class FxVecData(private val vec: Vector3d) : IFxData {
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeVec(vec)
	}
}
