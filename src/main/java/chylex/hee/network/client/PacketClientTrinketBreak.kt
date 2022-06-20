package chylex.hee.network.client

import chylex.hee.client.util.MC
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.network.BaseClientPacket
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer

class PacketClientTrinketBreak() : BaseClientPacket() {
	constructor(target: Entity, item: Item) : this() {
		this.entityId = target.entityId
		this.item = item
	}
	
	private var entityId: Int? = null
	private lateinit var item: Item
	
	override fun write(buffer: PacketBuffer) {
		buffer.writeInt(entityId!!)
		buffer.writeInt(Item.getIdFromItem(item))
	}
	
	override fun read(buffer: PacketBuffer) {
		entityId = buffer.readInt()
		item = Item.getItemById(buffer.readInt())
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		entityId?.let(player.world::getEntityByID)?.let {
			if (it === player) {
				MC.gameRenderer.displayItemActivation(ItemStack(item))
			}
			
			item.getHeeInterface<ITrinketItem>()?.spawnClientTrinketBreakFX(it)
		}
	}
}
