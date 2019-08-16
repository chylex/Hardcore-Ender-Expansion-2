package chylex.hee.game.container
import chylex.hee.game.container.util.DetectSlotChangeListener
import chylex.hee.game.item.ItemShulkerBoxOverride
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerShulkerBox

class ContainerShulkerBoxInInventory(player: EntityPlayer, private val boxInventory: ItemShulkerBoxOverride.Inventory) : ContainerShulkerBox(player.inventory, boxInventory, player){
	private val slotChangeListener = DetectSlotChangeListener()
	
	override fun detectAndSendChanges(){
		slotChangeListener.restart(listeners){ super.detectAndSendChanges() }?.let(boxInventory::validatePlayerItemOnModification)
	}
}
