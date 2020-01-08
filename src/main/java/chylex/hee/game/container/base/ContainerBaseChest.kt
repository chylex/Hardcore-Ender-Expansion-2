package chylex.hee.game.container.base
import chylex.hee.system.migration.vanilla.ContainerChest
import chylex.hee.system.migration.vanilla.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.ContainerType
import net.minecraft.inventory.container.Slot

abstract class ContainerBaseChest<T : IInventory>(type: ContainerType<out ContainerBaseChest<T>>, id: Int, player: EntityPlayer, chestInventory: IInventory, rows: Int) : ContainerChest(type, id, player.inventory, chestInventory, rows){
	override fun addSlot(slot: Slot): Slot{
		return if (slot.inventory === lowerChestInventory)
			super.addSlot(wrapChestSlot(slot))
		else
			super.addSlot(slot)
	}
	
	protected open fun wrapChestSlot(slot: Slot) = slot
}
