package chylex.hee.network.client

import chylex.hee.network.BaseClientPacket
import chylex.hee.util.buffer.use
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.potion.Effect

class PacketClientPotionDuration() : BaseClientPacket() {
	constructor(effect: Effect, newDuration: Int) : this() {
		this.effect = effect
		this.newDuration = newDuration
	}
	
	private var effect: Effect? = null
	private var newDuration: Int? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeRegistryId(effect!!)
		writeInt(newDuration!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		effect = readRegistryIdSafe(Effect::class.java)
		newDuration = readInt()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		effect?.let(player::getActivePotionEffect)?.duration = newDuration!!
	}
}
