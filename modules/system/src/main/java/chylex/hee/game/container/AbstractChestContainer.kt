package chylex.hee.game.container

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.ChestContainer
import net.minecraft.inventory.container.ContainerType
import net.minecraft.inventory.container.Slot

abstract class AbstractChestContainer<T : IInventory>(type: ContainerType<out AbstractChestContainer<T>>, id: Int, player: PlayerEntity, chestInventory: IInventory, rows: Int) : ChestContainer(type, id, player.inventory, chestInventory, rows) {
	override fun addSlot(slot: Slot): Slot {
		return if (slot.inventory === lowerChestInventory)
			super.addSlot(wrapChestSlot(slot))
		else
			super.addSlot(slot)
	}
	
	protected open fun wrapChestSlot(slot: Slot) = slot
}
