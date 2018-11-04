package chylex.hee.network.server
import chylex.hee.HEE
import chylex.hee.network.BaseServerPacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayerMP

class PacketServerContainerEvent() : BaseServerPacket(){
	interface IContainerWithEvents{
		fun handleContainerEvent(eventId: Byte)
	}
	
	// Instance
	
	constructor(eventId: Byte) : this(){
		this.eventId = eventId
	}
	
	private var eventId: Byte? = null
	
	override fun write(buffer: ByteBuf){
		buffer.writeByte(eventId!!.toInt())
	}
	
	override fun read(buffer: ByteBuf){
		eventId = buffer.readByte()
	}
	
	override fun handle(player: EntityPlayerMP){
		val container = player.openContainer
		
		if (container is IContainerWithEvents){
			eventId?.let { container.handleContainerEvent(it) }
		}
		else{
			HEE.log.warn("[PacketServerContainerEvent] handling client container event with incorrect 'openContainer' instance: $container")
		}
	}
}