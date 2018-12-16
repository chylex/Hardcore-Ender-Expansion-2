package chylex.hee.network.server
import chylex.hee.game.gui.slot.SlotTrinketItemInventory
import chylex.hee.network.BaseServerPacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack

class PacketServerShiftClickTrinket() : BaseServerPacket(){
	constructor(sourceSlot: Int) : this(){
		this.sourceSlot = sourceSlot
	}
	
	private var sourceSlot: Int? = null
	
	override fun write(buffer: ByteBuf){
		buffer.writeInt(sourceSlot!!)
	}
	
	override fun read(buffer: ByteBuf){
		sourceSlot = buffer.readInt()
	}
	
	override fun handle(player: EntityPlayerMP){
		player.markPlayerActive()
		
		val allSlots = player.inventoryContainer.inventorySlots
		
		val hoveredSlot = sourceSlot?.let(allSlots::getOrNull) ?: return
		val trinketSlot = SlotTrinketItemInventory.findTrinketSlot(allSlots) ?: return
		
		if (SlotTrinketItemInventory.canShiftClickTrinket(hoveredSlot, trinketSlot)){
			trinketSlot.putStack(hoveredSlot.stack)
			hoveredSlot.putStack(ItemStack.EMPTY)
		}
	}
}
