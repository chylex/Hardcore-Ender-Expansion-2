package chylex.hee.game.container.slot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class SlotWrapper(private val wrapped: Slot) : Slot(wrapped.inventory, wrapped.slotIndex, wrapped.xPos, wrapped.yPos){
	init{
		slotNumber = wrapped.slotNumber
	}
	
	@SideOnly(Side.CLIENT) override fun isEnabled() = wrapped.isEnabled
	
	override fun isHere(inventory: IInventory, slot: Int) = wrapped.isHere(inventory, slot)
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
	
	// TODO cannot access these in wrapped object
	override fun onCrafting(stack: ItemStack, amount: Int){}
	override fun onCrafting(stack: ItemStack){}
	override fun onSwapCraft(amount: Int){}
	
	override fun setBackgroundName(name: String?) = wrapped.setBackgroundName(name)
	@SideOnly(Side.CLIENT) override fun setBackgroundLocation(texture: ResourceLocation) = wrapped.setBackgroundLocation(texture)
	@SideOnly(Side.CLIENT) override fun getBackgroundLocation(): ResourceLocation = wrapped.backgroundLocation
	@SideOnly(Side.CLIENT) override fun getBackgroundSprite() = wrapped.backgroundSprite
	@SideOnly(Side.CLIENT) override fun getSlotTexture() = wrapped.slotTexture
}
