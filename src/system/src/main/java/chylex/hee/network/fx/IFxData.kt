package chylex.hee.network.fx
import net.minecraft.network.PacketBuffer

interface IFxData{
	fun write(buffer: PacketBuffer)
}
