package chylex.hee.network.client

import chylex.hee.network.BaseClientPacket
import chylex.hee.util.buffer.readVec
import chylex.hee.util.buffer.writeVec
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.vector.Vector3d

class PacketClientTeleportInstantly() : BaseClientPacket() {
	constructor(entity: Entity, position: Vector3d) : this() {
		this.entityId = entity.entityId
		this.position = position
	}
	
	private var entityId: Int? = null
	private lateinit var position: Vector3d
	
	override fun write(buffer: PacketBuffer) {
		buffer.writeInt(entityId!!)
		buffer.writeVec(position)
	}
	
	override fun read(buffer: PacketBuffer) {
		entityId = buffer.readInt()
		position = buffer.readVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		entityId?.let(player.world::getEntityByID)?.let {
			it.prevPosX = position.x
			it.prevPosY = position.y
			it.prevPosZ = position.z
			
			it.lastTickPosX = position.x
			it.lastTickPosY = position.y
			it.lastTickPosZ = position.z
			
			it.setPosition(position.x, position.y, position.z)
			it.setPositionAndRotationDirect(it.posX, it.posY, it.posZ, it.rotationYaw, it.rotationPitch, 0, true)
		}
	}
}
