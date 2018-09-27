package chylex.hee.network
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

interface IPacket{
	fun write(buffer: ByteBuf)
	fun read(buffer: ByteBuf)
	fun handle(side: Side, player: EntityPlayer)
}
