package chylex.hee.game.container.base
import chylex.hee.system.util.size
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

interface IContainerSlotTransferLogic{
	fun bridgeMergeItemStack(stack: ItemStack, startIndex: Int, endIndex: Int, reverseDirection: Boolean): Boolean
	
	@JvmDefault
	fun implTransferStackInSlot(inventorySlots: List<Slot>, containerInventory: IInventory, player: EntityPlayer, index: Int): ItemStack{
		val slot = inventorySlots[index]
		
		if (!slot.hasStack){
			return ItemStack.EMPTY
		}
		
		val modifiableStack = slot.stack
		val originalStack = modifiableStack.copy()
		
		if (index < containerInventory.size){
			if (!bridgeMergeItemStack(modifiableStack, containerInventory.size, inventorySlots.size, true)){
				return ItemStack.EMPTY
			}
		}
		else if (!bridgeMergeItemStack(modifiableStack, 0, containerInventory.size, false)){
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
}
