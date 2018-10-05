package chylex.hee.system.util
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

// Renaming

inline val IInventory.size
	get() = sizeInventory

inline fun IInventory.getStack(slot: Int): ItemStack{
	return getStackInSlot(slot)
}

inline fun IInventory.setStack(slot: Int, stack: ItemStack){
	setInventorySlotContents(slot, stack)
}

inline fun IInventory.reduceStack(slot: Int, amount: Int): ItemStack{
	return decrStackSize(slot, amount)
}

inline fun IInventory.isStackValid(slot: Int, stack: ItemStack): Boolean{
	return isItemValidForSlot(slot, stack)
}

// Iteration

data class InventorySlot(val slot: Int, val stack: ItemStack)

val IInventory.allSlots
	get() = object : Iterator<InventorySlot>{
		private val totalSlots = sizeInventory
		private var nextSlot = 0
		
		override fun hasNext(): Boolean{
			return nextSlot < totalSlots
		}
		
		override fun next(): InventorySlot{
			val currentSlot = nextSlot++
			return InventorySlot(currentSlot, getStack(currentSlot))
		}
	}

val IInventory.nonEmptySlots
	get() = object : AbstractIterator<InventorySlot>(){
		private val totalSlots = sizeInventory
		private var nextSlot = 0
		
		override fun computeNext(){
			while(nextSlot < totalSlots){
				val currentSlot = nextSlot++
				val stack = getStack(currentSlot)
				
				if (stack.isNotEmpty){
					setNext(InventorySlot(currentSlot, stack))
					return
				}
			}
			
			done()
		}
	}
