package chylex.hee.game.container.slot
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityPlayer
import net.minecraft.inventory.container.CraftingResultSlot
import net.minecraft.inventory.container.FurnaceResultSlot
import net.minecraft.inventory.container.MerchantResultSlot
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

abstract class SlotWrapper(private val wrapped: Slot) : Slot(wrapped.inventory, wrapped.slotIndex, wrapped.xPos, wrapped.yPos){
	init{
		slotNumber = wrapped.slotNumber
		
		if (wrapped is CraftingResultSlot || wrapped is FurnaceResultSlot || wrapped is MerchantResultSlot){
			// these types override onCrafting and onSwapCraft and wrapping them would change behavior
			throw UnsupportedOperationException("wrapping ${wrapped::class.java.simpleName} is not supported")
		}
	}
	
	@Sided(Side.CLIENT) override fun isEnabled() = wrapped.isEnabled
	
	override fun isSameInventory(other: Slot) = wrapped.isSameInventory(other)
	override fun getSlotIndex() = wrapped.slotIndex
	
	override fun isItemValid(stack: ItemStack) = wrapped.isItemValid(stack)
	override fun getItemStackLimit(stack: ItemStack) = wrapped.getItemStackLimit(stack)
	override fun canTakeStack(player: EntityPlayer) = wrapped.canTakeStack(player)
	override fun getSlotStackLimit() = wrapped.slotStackLimit
	
	override fun getHasStack() = wrapped.hasStack
	override fun getStack(): ItemStack = wrapped.stack
	override fun putStack(stack: ItemStack) = wrapped.putStack(stack)
	override fun decrStackSize(amount: Int): ItemStack = wrapped.decrStackSize(amount)
	
	override fun onTake(player: EntityPlayer, stack: ItemStack): ItemStack = wrapped.onTake(player, stack)
	override fun onSlotChange(modified: ItemStack, original: ItemStack) = wrapped.onSlotChange(modified, original)
	override fun onSlotChanged() = wrapped.onSlotChanged()
	
	override fun onCrafting(stack: ItemStack, amount: Int){}
	override fun onCrafting(stack: ItemStack){}
	override fun onSwapCraft(amount: Int){}
	
	@Sided(Side.CLIENT) override fun getBackground() = wrapped.background
	@Sided(Side.CLIENT) override fun setBackground(atlas: ResourceLocation, sprite: ResourceLocation): Slot = wrapped.setBackground(atlas, sprite)
}
