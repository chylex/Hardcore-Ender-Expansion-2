package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.util.readVec
import chylex.hee.system.util.use
import chylex.hee.system.util.writeVec
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class PacketClientTeleportInstantly() : BaseClientPacket(){
	constructor(entity: Entity, position: Vec3d) : this(){
		this.entityId = entity.entityId
		this.position = position
	}
	
	private var entityId: Int? = null
	private lateinit var position: Vec3d
	
	override fun write(buffer: ByteBuf) = buffer.use {
		writeInt(entityId!!)
		writeVec(position)
	}
	
	override fun read(buffer: ByteBuf) = buffer.use {
		entityId = readInt()
		position = readVec()
	}
	
	@SideOnly(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		entityId?.let(player.world::getEntityByID)?.setPositionAndUpdate(position.x, position.y, position.z)
	}
}
