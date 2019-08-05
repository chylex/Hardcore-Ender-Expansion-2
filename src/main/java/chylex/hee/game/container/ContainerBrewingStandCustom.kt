package chylex.hee.game.container
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom.Companion.SLOT_MODIFIER
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom.Companion.SLOT_REAGENT
import chylex.hee.game.container.base.IContainerSlotTransferLogic
import chylex.hee.game.container.slot.SlotBrewingModifier
import chylex.hee.game.container.slot.SlotBrewingReagent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ContainerBrewingStand
import net.minecraft.item.ItemStack

class ContainerBrewingStandCustom(inventory: InventoryPlayer, private val brewingStand: TileEntityBrewingStandCustom) : ContainerBrewingStand(inventory, brewingStand), IContainerSlotTransferLogic{
	init{
		SLOT_REAGENT.let { inventorySlots[it] = SlotBrewingReagent(inventorySlots[it], brewingStand.isEnhanced) }
		SLOT_MODIFIER.let { inventorySlots[it] = SlotBrewingModifier(inventorySlots[it]) }
	}
	
	override fun bridgeMergeItemStack(stack: ItemStack, startIndex: Int, endIndex: Int, reverseDirection: Boolean): Boolean{
		if (SLOT_MODIFIER in startIndex..endIndex && mergeItemStack(stack, SLOT_MODIFIER, SLOT_MODIFIER + 1, false)){
			return true
		}
		
		return mergeItemStack(stack, startIndex, endIndex, reverseDirection)
	}
	
	override fun transferStackInSlot(player: EntityPlayer, index: Int): ItemStack{
		return implTransferStackInSlot(inventorySlots, brewingStand, player, index)
	}
}
