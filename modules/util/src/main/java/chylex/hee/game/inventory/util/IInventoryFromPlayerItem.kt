package chylex.hee.game.inventory.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

interface IInventoryFromPlayerItem : IInventory {
	val player: PlayerEntity
	fun tryUpdatePlayerItem(): Boolean
	
	fun validatePlayerItemOnModification(modifiedSlot: Int) {
		if (!player.world.isRemote && !tryUpdatePlayerItem()) {
			if (modifiedSlot < this.size) {
				player.inventory.itemStack = ItemStack.EMPTY // prevent item duplication
			}
			
			player.closeScreen()
		}
	}
}
