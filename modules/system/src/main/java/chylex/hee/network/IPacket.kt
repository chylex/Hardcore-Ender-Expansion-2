package chylex.hee.network

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.LogicalSide

interface IPacket {
	fun write(buffer: PacketBuffer)
	fun read(buffer: PacketBuffer)
	fun handle(side: LogicalSide, player: PlayerEntity)
}
