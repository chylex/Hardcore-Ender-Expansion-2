package chylex.hee.game.container
import chylex.hee.game.container.base.ContainerBaseCustomInventory
import chylex.hee.game.container.slot.SlotTrinketItemNoContainers
import chylex.hee.game.container.util.DetectSlotChangeListener
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.size
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.wrapper.InvWrapper

class ContainerTrinketPouch(id: Int, player: EntityPlayer, slot: Int) : ContainerBaseCustomInventory<ItemTrinketPouch.Inv>(ModContainers.TRINKET_POUCH, id, player, ItemTrinketPouch.Inv(player, slot), HEIGHT){
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(id, inventory.player, buffer.readVarInt())
	
	companion object{
		const val HEIGHT = 132
		const val MAX_SLOTS = 5
	}
	
	private val slotChangeListener = DetectSlotChangeListener()
	
	override fun setupSlots(){
		val containerInventoryHandler = InvWrapper(containerInventory)
		val xStart = 80 - 18 * ((containerInventory.size - 1) / 2)
		
		for(slot in 0 until containerInventory.size){
			addSlot(SlotTrinketItemNoContainers(containerInventoryHandler, slot, xStart + (18 * slot), 18))
		}
	}
	
	override fun detectAndSendChanges(){
		slotChangeListener.restart(listeners){ super.detectAndSendChanges() }?.let(containerInventory::validatePlayerItemOnModification)
	}
}
