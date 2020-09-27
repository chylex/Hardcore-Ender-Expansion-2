package chylex.hee.network
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.LogicalSide

interface IPacket{
	fun write(buffer: PacketBuffer)
	fun read(buffer: PacketBuffer)
	fun handle(side: LogicalSide, player: EntityPlayer)
}
