package chylex.hee.game.container
import chylex.hee.game.container.base.ContainerBaseCustomInventory
import chylex.hee.game.container.slot.SlotTrinketItemNoContainers
import chylex.hee.game.container.util.DetectSlotChangeListener
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.system.util.size
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.items.wrapper.InvWrapper

class ContainerTrinketPouch(player: EntityPlayer, inventorySlot: Int) : ContainerBaseCustomInventory<ItemTrinketPouch.Inventory>(player, ItemTrinketPouch.Inventory(player, inventorySlot), HEIGHT){
	companion object{
		const val HEIGHT = 132
		const val MAX_SLOTS = 5
	}
	
	private val slotChangeListener = DetectSlotChangeListener()
	
	override fun setupSlots(){
		val containerInventoryHandler = InvWrapper(containerInventory)
		val xStart = 80 - 18 * ((containerInventory.size - 1) / 2)
		
		for(slot in 0 until containerInventory.size){
			addSlotToContainer(SlotTrinketItemNoContainers(containerInventoryHandler, slot, xStart + (18 * slot), 18))
		}
	}
	
	override fun detectAndSendChanges(){
		slotChangeListener.restart(listeners){ super.detectAndSendChanges() }?.let(containerInventory::validatePlayerItemOnModification)
	}
}
