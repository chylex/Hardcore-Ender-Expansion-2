package chylex.hee.game.fx
import chylex.hee.system.util.use
import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer

class FxEntityData(private val entity: Entity) : IFxData{
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeInt(entity.entityId)
	}
}
