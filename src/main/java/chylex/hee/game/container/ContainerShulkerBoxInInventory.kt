package chylex.hee.game.container
import chylex.hee.game.container.util.DetectSlotChangeListener
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.vanilla.ContainerShulkerBox
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.ContainerType

class ContainerShulkerBoxInInventory(id: Int, playerInventory: PlayerInventory, private val boxInventory: IInventory) : ContainerShulkerBox(id, playerInventory, boxInventory){
	constructor(id: Int, inventory: PlayerInventory) : this(id, inventory, Inventory(9 * 3)) // TODO different sizes
	
	private val slotChangeListener = DetectSlotChangeListener()
	
	override fun getType(): ContainerType<*>{
		return ModContainers.SHULKER_BOX_IN_INVENTORY
	}
	
	override fun detectAndSendChanges(){
		val inventory = boxInventory as? ItemShulkerBoxOverride.Inv
		
		if (inventory != null){ // UPDATE test
			slotChangeListener.restart(listeners){ super.detectAndSendChanges() }?.let(inventory::validatePlayerItemOnModification)
		}
	}
}
