package chylex.hee.game.container.base
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot

abstract class ContainerBaseChest<T : IInventory>(player: EntityPlayer, chestInventory: T) : ContainerChest(player.inventory, chestInventory, player){
	override fun addSlotToContainer(slot: Slot): Slot{
		return if (slot.inventory === lowerChestInventory)
			super.addSlotToContainer(wrapChestSlot(slot))
		else
			super.addSlotToContainer(slot)
	}
	
	protected open fun wrapChestSlot(slot: Slot) = slot
}
