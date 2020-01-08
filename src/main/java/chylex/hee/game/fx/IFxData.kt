package chylex.hee.game.fx
import net.minecraft.network.PacketBuffer

interface IFxData{
	fun write(buffer: PacketBuffer)
}
