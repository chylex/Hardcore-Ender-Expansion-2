package chylex.hee.game.container

import chylex.hee.game.inventory.util.IContainerSlotTransferLogic
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

@Suppress("LeakingThis")
abstract class AbstractCustomInventoryContainer<T : IInventory>(type: ContainerType<out AbstractCustomInventoryContainer<T>>, id: Int, player: PlayerEntity, val containerInventory: T, ySize: Int) : Container(type, id), IContainerSlotTransferLogic {
	init {
		containerInventory.openInventory(player)
		setupSlots()
		
		val playerInventory = player.inventory
		
		for (row in 0 until 3) {
			for (col in 0 until 9) {
				addSlot(Slot(playerInventory, 9 + col + (row * 9), 8 + (col * 18), ySize - 82 + (row * 18)))
			}
		}
		
		for (col in 0 until 9) {
			addSlot(Slot(playerInventory, col, 8 + (col * 18), ySize - 24))
		}
	}
	
	protected abstract fun setupSlots()
	
	override fun canInteractWith(player: PlayerEntity): Boolean {
		return containerInventory.isUsableByPlayer(player)
	}
	
	override fun bridgeMergeItemStack(stack: ItemStack, startIndex: Int, endIndex: Int, reverseDirection: Boolean): Boolean {
		return mergeItemStack(stack, startIndex, endIndex, reverseDirection)
	}
	
	override fun transferStackInSlot(player: PlayerEntity, index: Int): ItemStack {
		return implTransferStackInSlot(inventorySlots, containerInventory, player, index)
	}
	
	override fun onContainerClosed(player: PlayerEntity) {
		super.onContainerClosed(player)
		containerInventory.closeInventory(player)
	}
}
