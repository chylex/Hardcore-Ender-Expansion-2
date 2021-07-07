package chylex.hee.game.inventory.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemStackHandler

open class ItemStackHandlerInventory(private val handler: ItemStackHandler) : IInventory {
	private val slotIndices
		get() = 0 until handler.slots
	
	// Inventory (Properties)
	
	override fun getSizeInventory(): Int {
		return handler.slots
	}
	
	override fun getInventoryStackLimit(): Int {
		return handler.getSlotLimit(0)
	}
	
	override fun isEmpty(): Boolean {
		return slotIndices.all { handler.getStackInSlot(it).isEmpty }
	}
	
	// Inventory (Slots)
	
	override fun isItemValidForSlot(slot: Int, stack: ItemStack): Boolean {
		return handler.isItemValid(slot, stack)
	}
	
	override fun getStackInSlot(slot: Int): ItemStack {
		return handler.getStackInSlot(slot)
	}
	
	override fun setInventorySlotContents(slot: Int, stack: ItemStack) {
		handler.setStackInSlot(slot, stack)
	}
	
	override fun removeStackFromSlot(slot: Int): ItemStack {
		return handler.getStackInSlot(slot).also { handler.setStackInSlot(slot, ItemStack.EMPTY) }
	}
	
	override fun decrStackSize(slot: Int, count: Int): ItemStack {
		return handler.extractItem(slot, count, false)
	}
	
	override fun clear() {
		slotIndices.forEach { handler.setStackInSlot(it, ItemStack.EMPTY) }
	}
	
	override fun markDirty() {}
	
	// Interaction
	
	override fun isUsableByPlayer(player: PlayerEntity) = true
	override fun openInventory(player: PlayerEntity) {}
	override fun closeInventory(player: PlayerEntity) {}
}
