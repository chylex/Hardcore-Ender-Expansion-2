package chylex.hee.network.client

import chylex.hee.network.BaseClientPacket
import chylex.hee.util.buffer.readFloatVec
import chylex.hee.util.buffer.use
import chylex.hee.util.buffer.writeFloatVec
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.vector.Vector3d

class PacketClientLaunchInstantly() : BaseClientPacket() {
	constructor(entity: Entity, motion: Vector3d) : this() {
		this.entityId = entity.entityId
		this.motion = motion
	}
	
	private var entityId: Int? = null
	private lateinit var motion: Vector3d
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeInt(entityId!!)
		writeFloatVec(motion)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		entityId = readInt()
		motion = readFloatVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		entityId?.let(player.world::getEntityByID)?.motion = motion
	}
}
