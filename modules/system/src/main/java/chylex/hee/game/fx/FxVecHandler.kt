package chylex.hee.game.fx

import chylex.hee.util.buffer.readVec
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import java.util.Random

abstract class FxVecHandler : IFxHandler<FxVecData> {
	override fun handle(buffer: PacketBuffer, world: World, rand: Random) {
		handle(world, rand, buffer.readVec())
	}
	
	abstract fun handle(world: World, rand: Random, vec: Vector3d)
}
