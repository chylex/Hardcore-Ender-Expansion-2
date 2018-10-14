package chylex.hee.game.gui.slot
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class SlotReadOnly(inventory: IInventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y){
	constructor(wrapped: Slot) : this(wrapped.inventory, wrapped.slotIndex, wrapped.xPos, wrapped.yPos){
		slotNumber = wrapped.slotNumber
	}
	
	override fun isItemValid(stack: ItemStack): Boolean = false
}
