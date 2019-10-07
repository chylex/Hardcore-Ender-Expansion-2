package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.readFloatVec
import chylex.hee.system.util.use
import chylex.hee.system.util.writeFloatVec
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

class PacketClientLaunchInstantly() : BaseClientPacket(){
	constructor(entity: Entity, motion: Vec3d) : this(){
		this.entityId = entity.entityId
		this.motion = motion
	}
	
	private var entityId: Int? = null
	private lateinit var motion: Vec3d
	
	override fun write(buffer: ByteBuf) = buffer.use {
		writeInt(entityId!!)
		writeFloatVec(motion)
	}
	
	override fun read(buffer: ByteBuf) = buffer.use {
		entityId = readInt()
		motion = readFloatVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		entityId?.let(player.world::getEntityByID)?.motionVec = motion
	}
}
