package chylex.hee.network.client

import chylex.hee.network.BaseClientPacket
import chylex.hee.util.buffer.use
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketBuffer

class PacketClientRotateInstantly() : BaseClientPacket() {
	constructor(entity: Entity, yaw: Float, pitch: Float) : this() {
		this.entityId = entity.entityId
		this.yaw = yaw
		this.pitch = pitch
	}
	
	private var entityId: Int? = null
	private var yaw: Float? = null
	private var pitch: Float? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeInt(entityId!!)
		writeFloat(yaw!!)
		writeFloat(pitch!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		entityId = readInt()
		yaw = readFloat()
		pitch = readFloat()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		entityId?.let(player.world::getEntityByID)?.let {
			it.setPositionAndRotation(it.posX, it.posY, it.posZ, yaw!!, pitch!!)
			it.setRenderYawOffset(yaw!!)
			it.rotationYawHead = yaw!!
			
			if (it is LivingEntity) {
				it.prevRenderYawOffset = it.renderYawOffset
			}
		}
	}
}
