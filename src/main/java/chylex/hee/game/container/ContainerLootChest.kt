package chylex.hee.game.container
import chylex.hee.game.block.entity.TileEntityLootChest
import chylex.hee.game.container.slot.SlotReadOnly
import chylex.hee.system.util.size
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerChest

class ContainerLootChest(player: EntityPlayer, tile: TileEntityLootChest) : ContainerChest(player.inventory, tile.getChestInventoryFor(player), player){
	init{
		if (!player.isCreative){
			for(slot in 0 until lowerChestInventory.size){
				inventorySlots[slot] = SlotReadOnly(inventorySlots[slot])
			}
		}
	}
}
