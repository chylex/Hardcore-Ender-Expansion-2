package chylex.hee.network.fx

import net.minecraft.network.PacketBuffer
import net.minecraft.world.World
import java.util.Random

@Suppress("unused")
interface IFxHandler<T : IFxData> {
	fun handle(buffer: PacketBuffer, world: World, rand: Random)
}
