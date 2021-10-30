package chylex.hee.network.server

import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.game.inventory.util.getStack
import chylex.hee.game.item.container.IItemWithContainer
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModContainers
import chylex.hee.network.BaseServerPacket
import chylex.hee.util.buffer.use
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer

class PacketServerOpenInventoryItem() : BaseServerPacket() {
	constructor(slot: Int) : this() {
		this.slot = slot
	}
	
	private var slot: Int? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeVarInt(slot!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		slot = readVarInt()
	}
	
	override fun handle(player: ServerPlayerEntity) {
		val slot = slot!!
		
		val stack = if (slot == SlotTrinketItemInventory.INTERNAL_INDEX)
			TrinketHandler.getTrinketSlotItem(player)
		else
			player.inventory.getStack(slot)
		
		val open = stack.item.getHeeInterface<IItemWithContainer>()
		if (open != null) {
			ModContainers.open(player, open.createContainerProvider(stack, slot), slot)
		}
	}
}
