package chylex.hee.network
import chylex.hee.system.migration.forge.Side
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

interface IPacket{
	fun write(buffer: ByteBuf)
	fun read(buffer: ByteBuf)
	fun handle(side: Side, player: EntityPlayer)
}
