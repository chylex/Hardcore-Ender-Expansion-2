package chylex.hee.network.server

import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.network.BaseServerPacket
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer

class PacketServerShiftClickTrinket() : BaseServerPacket() {
	constructor(sourceSlot: Int) : this() {
		this.sourceSlot = sourceSlot
	}
	
	private var sourceSlot: Int? = null
	
	override fun write(buffer: PacketBuffer) {
		buffer.writeInt(sourceSlot!!)
	}
	
	override fun read(buffer: PacketBuffer) {
		sourceSlot = buffer.readInt()
	}
	
	override fun handle(player: ServerPlayerEntity) {
		player.markPlayerActive()
		
		val allSlots = player.container.inventorySlots
		
		val hoveredSlot = sourceSlot?.let(allSlots::getOrNull) ?: return
		val trinketSlot = SlotTrinketItemInventory.findTrinketSlot(allSlots) ?: return
		
		if (SlotTrinketItemInventory.canShiftClickTrinket(hoveredSlot, trinketSlot)) {
			trinketSlot.putStack(hoveredSlot.stack)
			hoveredSlot.putStack(ItemStack.EMPTY)
		}
	}
}
