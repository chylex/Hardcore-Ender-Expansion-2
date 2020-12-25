package chylex.hee.network.server

import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.network.BaseServerPacket
import chylex.hee.system.migration.EntityPlayerMP
import chylex.hee.system.serialization.use
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer

class PacketServerShiftClickTrinket() : BaseServerPacket() {
	constructor(sourceSlot: Int) : this() {
		this.sourceSlot = sourceSlot
	}
	
	private var sourceSlot: Int? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeInt(sourceSlot!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		sourceSlot = readInt()
	}
	
	override fun handle(player: EntityPlayerMP) {
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
