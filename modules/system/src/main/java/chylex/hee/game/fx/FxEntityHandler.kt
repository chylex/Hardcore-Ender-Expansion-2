package chylex.hee.game.fx

import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer
import net.minecraft.world.World
import java.util.Random

abstract class FxEntityHandler : IFxHandler<FxEntityData> {
	final override fun handle(buffer: PacketBuffer, world: World, rand: Random) {
		world.getEntityByID(buffer.readInt())?.let { handle(it, rand) }
	}
	
	abstract fun handle(entity: Entity, rand: Random)
}
