package chylex.hee.game.inventory

import chylex.hee.system.migration.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

interface IInventoryFromPlayerItem : IInventory {
	val player: EntityPlayer
	fun tryUpdatePlayerItem(): Boolean
	
	fun validatePlayerItemOnModification(modifiedSlot: Int) {
		if (!player.world.isRemote && !tryUpdatePlayerItem()) {
			if (modifiedSlot < size) {
				player.inventory.itemStack = ItemStack.EMPTY // prevent item duplication
			}
			
			player.closeScreen()
		}
	}
}
