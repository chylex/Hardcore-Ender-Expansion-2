package chylex.hee.game.container
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.container.slot.SlotTakeOnly
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.vanilla.ContainerChest
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.size
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory

class ContainerLootChest private constructor(id: Int, player: EntityPlayer, lootChest: IInventory) : ContainerChest(ModContainers.LOOT_CHEST, id, player.inventory, lootChest, 3){
	constructor(id: Int, player: EntityPlayer, tile: TileEntityLootChest) : this(id, player, tile.getChestInventoryFor(player))
	constructor(id: Int, inventory: PlayerInventory) : this(id, inventory.player, Inventory(9 * 3))
	
	init{
		if (!player.isCreative){
			for(slot in 0 until lowerChestInventory.size){
				inventorySlots[slot] = SlotTakeOnly(inventorySlots[slot])
			}
		}
	}
}
