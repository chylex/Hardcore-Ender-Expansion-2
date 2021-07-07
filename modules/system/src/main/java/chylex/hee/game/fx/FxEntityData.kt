package chylex.hee.game.fx

import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer

class FxEntityData(private val entity: Entity) : IFxData {
	override fun write(buffer: PacketBuffer) {
		buffer.writeInt(entity.entityId)
	}
}
