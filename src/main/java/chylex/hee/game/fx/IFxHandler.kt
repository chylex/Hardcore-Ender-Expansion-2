package chylex.hee.game.fx
import io.netty.buffer.ByteBuf
import net.minecraft.world.World
import java.util.Random

@Suppress("unused")
interface IFxHandler<T : IFxData>{
	fun handle(buffer: ByteBuf, world: World, rand: Random)
}
