package chylex.hee.game.fx
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity

class FxEntityData(private val entity: Entity) : IFxData{
	override fun write(buffer: ByteBuf) = buffer.use {
		writeInt(entity.entityId)
	}
}
