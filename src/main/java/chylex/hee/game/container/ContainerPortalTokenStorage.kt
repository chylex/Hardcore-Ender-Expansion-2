package chylex.hee.game.container

import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.container.slot.SlotFixValidityCheck
import chylex.hee.game.inventory.util.ItemStackHandlerInventory
import chylex.hee.game.inventory.util.nonEmptySlots
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.territory.system.storage.PlayerTokenStorage.ROWS
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModContainers
import chylex.hee.util.buffer.readPos
import chylex.hee.util.collection.any
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.ClickType
import net.minecraft.inventory.container.ClickType.PICKUP
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer

class ContainerPortalTokenStorage(id: Int, player: PlayerEntity, storageInventory: IInventory, private val tile: TileEntityVoidPortalStorage?) : AbstractChestContainer<ItemStackHandlerInventory>(ModContainers.PORTAL_TOKEN_STORAGE, id, player, storageInventory, ROWS) {
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(id, inventory.player, Inventory(9 * ROWS), buffer.readPos().getTile(inventory.player.world))
	
	override fun wrapChestSlot(slot: Slot): Slot {
		return SlotFixValidityCheck(slot)
	}
	
	override fun slotClick(slot: Int, mouseButton: Int, clickType: ClickType, player: PlayerEntity): ItemStack {
		if (mouseButton == 1 && clickType == PICKUP) {
			val stack = getSlot(slot).stack
			
			if (stack.isNotEmpty) {
				if (!player.world.isRemote && canActivateToken(stack)) {
					tile?.activateToken(stack)
					player.closeScreen()
				}
				
				return ItemStack.EMPTY
			}
		}
		
		return super.slotClick(slot, mouseButton, clickType, player)
	}
	
	fun canActivateToken(stack: ItemStack): Boolean {
		return lowerChestInventory.nonEmptySlots.any { it.stack === stack }
	}
	
	override fun canInteractWith(player: PlayerEntity) = true
}
