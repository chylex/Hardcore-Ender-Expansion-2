package chylex.hee.game.container.util
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.items.ItemStackHandler

open class ItemStackHandlerInventory(private val handler: ItemStackHandler, private val name: String, private val hasCustomName: Boolean = false) : IInventory{
	private val slotIndices
		get() = 0 until handler.slots
	
	// Inventory (Properties)
	
	override fun getSizeInventory(): Int{
		return handler.slots
	}
	
	override fun getInventoryStackLimit(): Int{
		return handler.getSlotLimit(0)
	}
	
	override fun isEmpty(): Boolean{
		return slotIndices.all { handler.getStackInSlot(it).isEmpty }
	}
	
	// Inventory (Slots)
	
	override fun isItemValidForSlot(slot: Int, stack: ItemStack): Boolean{
		return handler.isItemValid(slot, stack)
	}
	
	override fun getStackInSlot(slot: Int): ItemStack{
		return handler.getStackInSlot(slot)
	}
	
	override fun setInventorySlotContents(slot: Int, stack: ItemStack){
		handler.setStackInSlot(slot, stack)
	}
	
	override fun removeStackFromSlot(slot: Int): ItemStack{
		return handler.getStackInSlot(slot).also { handler.setStackInSlot(slot, ItemStack.EMPTY) }
	}
	
	override fun decrStackSize(slot: Int, count: Int): ItemStack{
		return handler.extractItem(slot, count, false)
	}
	
	override fun clear(){
		slotIndices.forEach { handler.setStackInSlot(it, ItemStack.EMPTY) }
	}
	
	override fun markDirty(){}
	
	// Name
	
	override fun getName() = name
	override fun hasCustomName() = hasCustomName
	
	override fun getDisplayName() =
		if (hasCustomName)
			TextComponentString(name)
		else
			TextComponentTranslation(name)
	
	// Interaction
	
	override fun isUsableByPlayer(player: EntityPlayer) = true
	override fun openInventory(player: EntityPlayer){}
	override fun closeInventory(player: EntityPlayer){}
	
	// Fields
	
	override fun setField(id: Int, value: Int){}
	override fun getField(id: Int) = 0
	override fun getFieldCount() = 0
}
