package chylex.hee.game.container.base
import chylex.hee.system.util.size
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

abstract class ContainerBaseCustomInventory<T : IInventory>(player: EntityPlayer, val containerInventory: T, ySize: Int) : Container(){
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
	
	override fun transferStackInSlot(player: EntityPlayer, index: Int): ItemStack{
		val slot = inventorySlots[index]
		
		if (slot == null || !slot.hasStack){
			return ItemStack.EMPTY
		}
		
		val modifiableStack = slot.stack
		val originalStack = modifiableStack.copy()
		
		if (index < containerInventory.size){
			if (!mergeItemStack(modifiableStack, containerInventory.size, inventorySlots.size, true)){
				return ItemStack.EMPTY
			}
		}
		else if (!mergeItemStack(modifiableStack, 0, containerInventory.size, false)){
			return ItemStack.EMPTY
		}
		
		if (modifiableStack.isEmpty){
			slot.putStack(ItemStack.EMPTY)
		}
		else{
			slot.onSlotChanged()
		}
		
		return originalStack
	}
	
	override fun onContainerClosed(player: EntityPlayer){
		super.onContainerClosed(player)
		containerInventory.closeInventory(player)
	}
}
