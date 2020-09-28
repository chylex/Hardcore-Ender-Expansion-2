package chylex.hee.game.container
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom.Companion.SLOT_MODIFIER
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom.Companion.SLOT_REAGENT
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom.Companion.TOTAL_FIELDS
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom.Companion.TOTAL_SLOTS
import chylex.hee.game.container.slot.SlotBrewingModifier
import chylex.hee.game.container.slot.SlotBrewingReagent
import chylex.hee.game.world.getTile
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.serialization.readPos
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.BrewingStandContainer
import net.minecraft.inventory.container.ContainerType
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.util.IIntArray
import net.minecraft.util.IntArray

class ContainerBrewingStandCustom(id: Int, inventory: PlayerInventory, private val brewingStand: IInventory, fields: IIntArray, tile: TileEntityBrewingStandCustom?) : BrewingStandContainer(id, inventory, brewingStand, fields), IContainerSlotTransferLogic{
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(id, inventory, Inventory(TOTAL_SLOTS), IntArray(TOTAL_FIELDS), buffer.readPos().getTile(inventory.player.world))
	
	init{
		SLOT_REAGENT.let { inventorySlots[it] = SlotBrewingReagent(inventorySlots[it], tile?.isEnhanced == true) }
		SLOT_MODIFIER.let { inventorySlots[it] = SlotBrewingModifier(inventorySlots[it]) }
		
		brewingStand.openInventory(inventory.player)
	}
	
	override fun getType(): ContainerType<*>{
		return ModContainers.BREWING_STAND
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
	
	override fun onContainerClosed(player: EntityPlayer){
		super.onContainerClosed(player)
		brewingStand.closeInventory(player)
	}
}
