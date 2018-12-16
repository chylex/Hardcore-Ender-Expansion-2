package chylex.hee.network.server
import chylex.hee.init.ModGuiHandler.GuiType
import chylex.hee.network.BaseServerPacket
import chylex.hee.system.util.readVarInt
import chylex.hee.system.util.writeVarInt
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayerMP

class PacketServerOpenGui() : BaseServerPacket(){
	constructor(type: GuiType, x: Int = 0, y: Int = 0, z: Int = 0) : this(){
		this.type = type
		this.x = x
		this.y = y
		this.z = z
	}
	
	private var type: GuiType? = null
	private var x: Int? = null
	private var y: Int? = null
	private var z: Int? = null
	
	override fun write(buffer: ByteBuf){
		buffer.writeVarInt(type!!.ordinal)
		buffer.writeVarInt(x!!)
		buffer.writeVarInt(y!!)
		buffer.writeVarInt(z!!)
	}
	
	override fun read(buffer: ByteBuf){
		type = GuiType.values().getOrNull(buffer.readVarInt())
		x = buffer.readVarInt()
		y = buffer.readVarInt()
		z = buffer.readVarInt()
	}
	
	override fun handle(player: EntityPlayerMP){
		type?.open(player, x!!, y!!, z!!)
	}
}
