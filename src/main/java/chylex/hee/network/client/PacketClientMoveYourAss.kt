package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.readVec
import chylex.hee.system.util.use
import chylex.hee.system.util.writeVec
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.math.Vec3d

class PacketClientMoveYourAss() : BaseClientPacket(){
	constructor(position: Vec3d) : this(){
		this.position = position
	}
	
	private lateinit var position: Vec3d
	
	override fun write(buffer: ByteBuf) = buffer.use {
		writeVec(position)
	}
	
	override fun read(buffer: ByteBuf) = buffer.use {
		position = readVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		player.dismountRidingEntity()
		player.setLocationAndAngles(position.x, position.y, position.z, player.rotationYaw, player.rotationPitch)
	}
}
