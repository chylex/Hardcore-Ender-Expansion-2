package chylex.hee.game.inventory.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

interface IContainerSlotTransferLogic {
	fun bridgeMergeItemStack(stack: ItemStack, startIndex: Int, endIndex: Int, reverseDirection: Boolean): Boolean
	
	fun implTransferStackInSlot(inventorySlots: List<Slot>, containerInventory: IInventory, player: PlayerEntity, index: Int): ItemStack {
		val slot = inventorySlots[index]
		
		if (!slot.hasStack) {
			return ItemStack.EMPTY
		}
		
		val modifiableStack = slot.stack
		val originalStack = modifiableStack.copy()
		
		if (index < containerInventory.size) {
			if (!bridgeMergeItemStack(modifiableStack, containerInventory.size, inventorySlots.size, true)) {
				return ItemStack.EMPTY
			}
		}
		else if (!bridgeMergeItemStack(modifiableStack, 0, containerInventory.size, false)) {
			return ItemStack.EMPTY
		}
		
		if (modifiableStack.isEmpty) {
			slot.putStack(ItemStack.EMPTY)
		}
		else {
			slot.onSlotChanged()
		}
		
		return originalStack
	}
}
