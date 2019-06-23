package chylex.hee.network.client
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.potion.Potion
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class PacketClientPotionDuration() : BaseClientPacket(){
	constructor(potion: Potion, newDuration: Int) : this(){
		this.potion = potion
		this.newDuration = newDuration
	}
	
	private var potion: Potion? = null
	private var newDuration: Int? = null
	
	override fun write(buffer: ByteBuf) = buffer.use {
		writeInt(Potion.REGISTRY.getIDForObject(potion))
		writeInt(newDuration!!)
	}
	
	override fun read(buffer: ByteBuf) = buffer.use {
		potion = Potion.REGISTRY.getObjectById(readInt())
		newDuration = readInt()
	}
	
	@SideOnly(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		potion?.let(player::getActivePotionEffect)?.duration = newDuration!!
	}
}
