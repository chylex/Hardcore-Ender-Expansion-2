package chylex.hee.network.client

import chylex.hee.network.BaseClientPacket
import chylex.hee.util.buffer.readVec
import chylex.hee.util.buffer.writeVec
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.vector.Vector3d

class PacketClientMoveYourAss() : BaseClientPacket() {
	constructor(position: Vector3d) : this() {
		this.position = position
	}
	
	private lateinit var position: Vector3d
	
	override fun write(buffer: PacketBuffer) {
		buffer.writeVec(position)
	}
	
	override fun read(buffer: PacketBuffer) {
		position = buffer.readVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		player.stopRiding()
		player.setLocationAndAngles(position.x, position.y, position.z, player.rotationYaw, player.rotationPitch)
	}
}
