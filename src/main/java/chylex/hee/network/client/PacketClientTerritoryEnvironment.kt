package chylex.hee.network.client

import chylex.hee.game.territory.TerritoryVoid
import chylex.hee.game.territory.storage.VoidData
import chylex.hee.game.territory.system.storage.TerritoryEntry
import chylex.hee.network.BaseClientPacket
import chylex.hee.util.buffer.readTag
import chylex.hee.util.buffer.use
import chylex.hee.util.buffer.writeTag
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.nbt.TagCompound
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.network.PacketBuffer

class PacketClientTerritoryEnvironment() : BaseClientPacket() {
	constructor(entry: TerritoryEntry) : this() {
		this.void = entry.getComponent<VoidData>()?.serializeNBT()
	}
	
	private var void: TagCompound? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeOptionalTag(void)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		void = readOptionalTag()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		TerritoryVoid.CLIENT_VOID_DATA.deserializeNBT(void ?: TagCompound())
	}
	
	private fun PacketBuffer.writeOptionalTag(tag: TagCompound?) {
		if (tag == null) {
			writeBoolean(false)
		}
		else {
			writeBoolean(true)
			writeTag(tag)
		}
	}
	
	private fun PacketBuffer.readOptionalTag(): TagCompound? {
		return if (readBoolean()) readTag() else null
	}
}
