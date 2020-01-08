package chylex.hee.game.container
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.container.base.ContainerBaseChest
import chylex.hee.game.container.slot.SlotFixValidityCheck
import chylex.hee.game.container.util.ItemStackHandlerInventory
import chylex.hee.game.world.territory.storage.TokenPlayerStorage.ROWS
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.getTile
import chylex.hee.system.util.isNotEmpty
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.ClickType
import net.minecraft.inventory.container.ClickType.PICKUP
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.IWorldPosCallable

class ContainerPortalTokenStorage(id: Int, player: EntityPlayer, storageInventory: IInventory, private val tile: IWorldPosCallable) : ContainerBaseChest<ItemStackHandlerInventory>(ModContainers.PORTAL_TOKEN_STORAGE, id, player, storageInventory, ROWS){
	/* UPDATE player, ItemStackHandlerInventory(TokenPlayerStorage.forPlayer(player), "gui.hee.portal_token_storage.title") */
	constructor(id: Int, inventory: PlayerInventory) : this(id, inventory.player, Inventory(9 * ROWS), IWorldPosCallable.DUMMY)
	
	override fun wrapChestSlot(slot: Slot): Slot{
		return SlotFixValidityCheck(slot)
	}
	
	override fun slotClick(slot: Int, mouseButton: Int, clickType: ClickType, player: EntityPlayer): ItemStack{
		if (mouseButton == 1 && clickType == PICKUP){
			val stack = getSlot(slot).stack
			
			if (stack.isNotEmpty){
				if (!player.world.isRemote){
					tile.apply { world, pos -> pos.getTile<TileEntityVoidPortalStorage>(world) }.orElse(null)?.activateToken(stack)
					player.closeScreen()
				}
				
				return ItemStack.EMPTY
			}
		}
		
		return super.slotClick(slot, mouseButton, clickType, player)
	}
	
	override fun canInteractWith(player: EntityPlayer) = true
}
