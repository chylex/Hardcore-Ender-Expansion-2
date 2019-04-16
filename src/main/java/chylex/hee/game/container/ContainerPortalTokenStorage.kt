package chylex.hee.game.container
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.container.base.ContainerBaseChest
import chylex.hee.game.container.slot.SlotFixValidityCheck
import chylex.hee.game.container.util.ItemStackHandlerInventory
import chylex.hee.game.world.territory.storage.TokenPlayerStorage
import chylex.hee.system.util.getStack
import chylex.hee.system.util.isNotEmpty
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.ClickType.PICKUP
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class ContainerPortalTokenStorage(player: EntityPlayer, private val tile: TileEntityVoidPortalStorage) : ContainerBaseChest<ItemStackHandlerInventory>(player, ItemStackHandlerInventory(TokenPlayerStorage.forPlayer(player), "gui.hee.portal_token_storage.title")){
	override fun wrapChestSlot(slot: Slot): Slot{
		return SlotFixValidityCheck(slot)
	}
	
	override fun slotClick(slot: Int, mouseButton: Int, clickType: ClickType, player: EntityPlayer): ItemStack{
		if (mouseButton == 1 && clickType == PICKUP){
			val stack = lowerChestInventory.getStack(slot)
			
			if (stack.isNotEmpty && !player.world.isRemote){
				tile.activateToken(stack)
				player.closeScreen()
			}
			
			return ItemStack.EMPTY
		}
		
		return super.slotClick(slot, mouseButton, clickType, player)
	}
	
	override fun canInteractWith(player: EntityPlayer) = true
}
