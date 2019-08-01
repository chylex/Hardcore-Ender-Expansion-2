package chylex.hee.game.container.base
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

abstract class ContainerBaseCustomInventory<T : IInventory>(player: EntityPlayer, val containerInventory: T, ySize: Int) : Container(), IContainerSlotTransferLogic{
	init{
		containerInventory.openInventory(player)
		setupSlots()
		
		val playerInventory = player.inventory
		
		for(row in 0 until 3){
			for(col in 0 until 9){
				addSlotToContainer(Slot(playerInventory, 9 + col + (row * 9), 8 + (col * 18), ySize - 82 + (row * 18)))
			}
		}
		
		for(col in 0 until 9){
			addSlotToContainer(Slot(playerInventory, col, 8 + (col * 18), ySize - 24))
		}
	}
	
	protected abstract fun setupSlots()
	
	override fun canInteractWith(player: EntityPlayer): Boolean{
		return containerInventory.isUsableByPlayer(player)
	}
	
	override fun bridgeMergeItemStack(stack: ItemStack, startIndex: Int, endIndex: Int, reverseDirection: Boolean): Boolean{
		return mergeItemStack(stack, startIndex, endIndex, reverseDirection)
	}
	
	override fun transferStackInSlot(player: EntityPlayer, index: Int): ItemStack{
		return implTransferStackInSlot(inventorySlots, containerInventory, player, index)
	}
	
	override fun onContainerClosed(player: EntityPlayer){
		super.onContainerClosed(player)
		containerInventory.closeInventory(player)
	}
}
