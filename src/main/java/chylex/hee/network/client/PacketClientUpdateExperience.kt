package chylex.hee.network.client

import chylex.hee.network.BaseClientPacket
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.network.PacketBuffer

class PacketClientUpdateExperience() : BaseClientPacket() {
	constructor(experience: Float) : this() {
		this.experience = experience
	}
	
	private var experience: Float? = null
	
	override fun write(buffer: PacketBuffer) {
		buffer.writeFloat(experience!!)
	}
	
	override fun read(buffer: PacketBuffer) {
		experience = buffer.readFloat()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		player.experience = experience!!
	}
}
