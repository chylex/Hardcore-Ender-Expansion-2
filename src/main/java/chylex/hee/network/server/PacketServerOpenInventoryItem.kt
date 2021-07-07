package chylex.hee.network.server

import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.game.inventory.util.getStack
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.game.item.ItemTrinketPouch
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
		
		when (stack.item) {
			is ItemTrinketPouch       -> ModContainers.open(player, ItemTrinketPouch.ContainerProvider(stack, slot), slot)
			is ItemShulkerBoxOverride -> ModContainers.open(player, ItemShulkerBoxOverride.ContainerProvider(stack, slot), slot)
		}
	}
}
