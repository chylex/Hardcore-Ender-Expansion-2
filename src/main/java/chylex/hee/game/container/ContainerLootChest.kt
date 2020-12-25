package chylex.hee.game.container

import chylex.hee.game.block.entity.TileEntityLootChest.Companion.ROWS
import chylex.hee.game.container.slot.SlotTakeOnly
import chylex.hee.game.inventory.size
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.ChestContainer
import net.minecraft.network.PacketBuffer

class ContainerLootChest(id: Int, player: EntityPlayer, lootChest: IInventory) : ChestContainer(ModContainers.LOOT_CHEST, id, player.inventory, lootChest, ROWS) {
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, @Suppress("UNUSED_PARAMETER") buffer: PacketBuffer) : this(id, inventory.player, Inventory(9 * ROWS))
	
	init {
		if (!player.isCreative) {
			for(slot in 0 until lowerChestInventory.size) {
				inventorySlots[slot] = SlotTakeOnly(inventorySlots[slot])
			}
		}
	}
}
