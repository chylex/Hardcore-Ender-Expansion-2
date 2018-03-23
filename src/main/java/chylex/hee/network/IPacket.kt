package chylex.hee.network
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.relauncher.Side

interface IPacket{
	fun write(buffer: PacketBuffer)
	fun read(buffer: PacketBuffer)
	fun handle(side: Side, player: EntityPlayer)
}
