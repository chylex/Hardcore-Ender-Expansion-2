package chylex.hee.game.container.base
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.size
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

interface IInventoryFromPlayerItem : IInventory{
	val player: EntityPlayer
	fun tryUpdatePlayerItem(): Boolean
	
	@JvmDefault
	fun validatePlayerItemOnModification(modifiedSlot: Int){
		if (!player.world.isRemote && !tryUpdatePlayerItem()){
			if (modifiedSlot < size){
				player.inventory.itemStack = ItemStack.EMPTY // prevent item duplication
			}
			
			player.closeScreen()
		}
	}
}
