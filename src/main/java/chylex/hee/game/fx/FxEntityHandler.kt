package chylex.hee.game.fx
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.world.World
import java.util.Random

abstract class FxEntityHandler : IFxHandler<FxEntityData>{
	final override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
		world.getEntityByID(readInt())?.let { handle(it, rand) }
	}
	
	abstract fun handle(entity: Entity, rand: Random)
}
