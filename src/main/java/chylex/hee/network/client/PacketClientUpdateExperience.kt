package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityPlayerSP
import chylex.hee.system.util.use
import net.minecraft.network.PacketBuffer

class PacketClientUpdateExperience() : BaseClientPacket(){
	constructor(experience: Float) : this(){
		this.experience = experience
	}
	
	private var experience: Float? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeFloat(experience!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		experience = readFloat()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		player.experience = experience!!
	}
}
