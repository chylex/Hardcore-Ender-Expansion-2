package chylex.hee.network.client
import chylex.hee.game.world.territory.storage.TerritoryEntry
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.readTag
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeTag
import net.minecraft.network.PacketBuffer

class PacketClientTerritoryEnvironment() : BaseClientPacket(){
	constructor(entry: TerritoryEntry) : this(){
	}
	
	override fun write(buffer: PacketBuffer) = buffer.use {
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
	}
	
	private fun PacketBuffer.writeOptionalTag(tag: TagCompound?){
		if (tag == null){
			writeBoolean(false)
		}
		else{
			writeBoolean(true)
			writeTag(tag)
		}
	}
	
	private fun PacketBuffer.readOptionalTag(): TagCompound?{
		return if (readBoolean()) readTag() else null
	}
}
