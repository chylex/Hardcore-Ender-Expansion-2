package chylex.hee.network.fx

import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeVec
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.Vec3d

class FxVecData(private val vec: Vec3d) : IFxData {
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeVec(vec)
	}
}
