package chylex.hee.network.fx

import chylex.hee.system.serialization.readVec
import chylex.hee.system.serialization.use
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random

abstract class FxVecHandler : IFxHandler<FxVecData> {
	override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
		handle(world, rand, readVec())
	}
	
	abstract fun handle(world: World, rand: Random, vec: Vec3d)
}
