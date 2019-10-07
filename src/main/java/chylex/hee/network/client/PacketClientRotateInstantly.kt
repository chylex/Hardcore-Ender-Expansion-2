package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity

class PacketClientRotateInstantly() : BaseClientPacket(){
	constructor(entity: Entity, yaw: Float, pitch: Float) : this(){
		this.entityId = entity.entityId
		this.yaw = yaw
		this.pitch = pitch
	}
	
	private var entityId: Int? = null
	private var yaw: Float? = null
	private var pitch: Float? = null
	
	override fun write(buffer: ByteBuf) = buffer.use {
		writeInt(entityId!!)
		writeFloat(yaw!!)
		writeFloat(pitch!!)
	}
	
	override fun read(buffer: ByteBuf) = buffer.use {
		entityId = readInt()
		yaw = readFloat()
		pitch = readFloat()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		entityId?.let(player.world::getEntityByID)?.let {
			it.setPositionAndRotation(it.posX, it.posY, it.posZ, yaw!!, pitch!!)
			it.setRenderYawOffset(yaw!!)
			it.rotationYawHead = yaw!!
		}
	}
}
