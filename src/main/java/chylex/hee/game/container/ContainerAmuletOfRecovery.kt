package chylex.hee.game.container
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.container.slot.SlotTakeOnly
import chylex.hee.game.container.util.DetectSlotChangeListener
import chylex.hee.game.item.ItemAmuletOfRecovery
import chylex.hee.init.ModContainers
import chylex.hee.network.server.PacketServerContainerEvent.IContainerWithEvents
import chylex.hee.system.migration.vanilla.ContainerChest
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.size
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory

class ContainerAmuletOfRecovery(id: Int, private val player: EntityPlayer, amuletInventory: IInventory) : ContainerChest(ModContainers.AMULET_OF_RECOVERY, id, player.inventory, amuletInventory, 3), IContainerWithEvents{
	constructor(id: Int, player: EntityPlayer, tile: TileEntityLootChest) : this(id, player, tile.getChestInventoryFor(player))
	constructor(id: Int, inventory: PlayerInventory) : this(id, inventory.player, Inventory(9 * 3))
	
	private val slotChangeListener = DetectSlotChangeListener()
	
	private val amuletInventory: ItemAmuletOfRecovery.Inv
		get() = lowerChestInventory as ItemAmuletOfRecovery.Inv
	
	init{
		for(slot in 0 until amuletInventory.size){
			inventorySlots[slot] = SlotTakeOnly(inventorySlots[slot]).apply {
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
		slotChangeListener.restart(listeners){ super.detectAndSendChanges() }?.let(amuletInventory::validatePlayerItemOnModification)
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
