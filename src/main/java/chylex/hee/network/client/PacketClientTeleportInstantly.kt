package chylex.hee.network.client

import chylex.hee.network.BaseClientPacket
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import chylex.hee.system.serialization.readVec
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeVec
import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.Vec3d

class PacketClientTeleportInstantly() : BaseClientPacket() {
	constructor(entity: Entity, position: Vec3d) : this() {
		this.entityId = entity.entityId
		this.position = position
	}
	
	private var entityId: Int? = null
	private lateinit var position: Vec3d
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeInt(entityId!!)
		writeVec(position)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		entityId = readInt()
		position = readVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP) {
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
