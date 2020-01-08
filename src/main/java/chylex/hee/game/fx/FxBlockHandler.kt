package chylex.hee.game.fx
import chylex.hee.system.util.readPos
import chylex.hee.system.util.use
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

abstract class FxBlockHandler : IFxHandler<FxBlockData>{
	final override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
		handle(readPos(), world, rand)
	}
	
	abstract fun handle(pos: BlockPos, world: World, rand: Random)
}
