package chylex.hee.game.container
import chylex.hee.game.container.slot.SlotReadOnly
import chylex.hee.game.container.util.DetectSlotChangeListener
import chylex.hee.game.item.ItemAmuletOfRecovery
import chylex.hee.network.server.PacketServerContainerEvent.IContainerWithEvents
import chylex.hee.system.util.size
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand

class ContainerAmuletOfRecovery(private val player: EntityPlayer, itemHeldIn: EnumHand) : ContainerChest(player.inventory, ItemAmuletOfRecovery.Inventory(player, itemHeldIn), player), IContainerWithEvents{
	private val slotChangeListener = DetectSlotChangeListener()
	
	private val amuletInventory: ItemAmuletOfRecovery.Inventory
		get() = super.getLowerChestInventory() as ItemAmuletOfRecovery.Inventory
	
	init{
		for(slot in 0 until amuletInventory.size){
			inventorySlots[slot] = SlotReadOnly(inventorySlots[slot]).apply {
				val row = slot / 9
				
				if (row == 0){ // move hotbar row to bottom
					yPos += 4 * 18
				}
				else if (row == 4){ // move armor/offhand/other row to top
					yPos -= 4 * 18
					
					when(slot % 9){ // reverse order of armor slots
						0 -> xPos += 3 * 18
						1 -> xPos += 1 * 18
						2 -> xPos -= 1 * 18
						3 -> xPos -= 3 * 18
					}
				}
			}
		}
	}
	
	override fun detectAndSendChanges(){
		val modifiedSlot = slotChangeListener.restart(listeners){
			super.detectAndSendChanges()
		}
		
		if (modifiedSlot != null && !player.world.isRemote && !amuletInventory.tryUpdateHeldItem()){
			if (modifiedSlot < amuletInventory.size){
				player.inventory.itemStack = ItemStack.EMPTY // prevent item duplication
			}
			
			player.closeScreen()
		}
	}
	
	override fun handleContainerEvent(eventId: Byte){
		if (eventId.toInt() == 0 && !player.world.isRemote){
			if (amuletInventory.moveToPlayerInventory()){
				detectAndSendChanges()
			}
			else{
				player.closeScreen()
			}
		}
	}
	
	override fun canInteractWith(player: EntityPlayer) = true
}
