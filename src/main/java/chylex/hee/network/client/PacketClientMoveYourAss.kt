package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import chylex.hee.system.serialization.readVec
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeVec
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.Vec3d

class PacketClientMoveYourAss() : BaseClientPacket(){
	constructor(position: Vec3d) : this(){
		this.position = position
	}
	
	private lateinit var position: Vec3d
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeVec(position)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		position = readVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		player.stopRiding()
		player.setLocationAndAngles(position.x, position.y, position.z, player.rotationYaw, player.rotationPitch)
	}
}
