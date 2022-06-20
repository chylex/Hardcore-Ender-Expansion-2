package chylex.hee.network.server

import chylex.hee.HEE
import chylex.hee.network.BaseServerPacket
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer

class PacketServerContainerEvent() : BaseServerPacket() {
	interface IContainerWithEvents {
		fun handleContainerEvent(eventId: Byte)
	}
	
	// Instance
	
	constructor(eventId: Byte) : this() {
		this.eventId = eventId
	}
	
	private var eventId: Byte? = null
	
	override fun write(buffer: PacketBuffer) {
		buffer.writeByte(eventId!!.toInt())
	}
	
	override fun read(buffer: PacketBuffer) {
		eventId = buffer.readByte()
	}
	
	override fun handle(player: ServerPlayerEntity) {
		val container = player.openContainer
		
		if (container is IContainerWithEvents) {
			eventId?.let(container::handleContainerEvent)
		}
		else {
			HEE.log.warn("[PacketServerContainerEvent] handling client container event with incorrect 'openContainer' instance: $container")
		}
	}
}
