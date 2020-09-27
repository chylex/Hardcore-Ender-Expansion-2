package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import chylex.hee.system.migration.Potion
import chylex.hee.system.serialization.use
import net.minecraft.network.PacketBuffer

class PacketClientPotionDuration() : BaseClientPacket(){
	constructor(potion: Potion, newDuration: Int) : this(){
		this.potion = potion
		this.newDuration = newDuration
	}
	
	private var potion: Potion? = null
	private var newDuration: Int? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeRegistryId(potion!!)
		writeInt(newDuration!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		potion = readRegistryIdSafe(Potion::class.java)
		newDuration = readInt()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		potion?.let(player::getActivePotionEffect)?.duration = newDuration!!
	}
}
