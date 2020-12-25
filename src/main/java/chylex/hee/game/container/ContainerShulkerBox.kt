package chylex.hee.game.container

import chylex.hee.game.container.base.ContainerBaseChest
import chylex.hee.game.container.slot.SlotShulkerBox
import chylex.hee.game.inventory.size
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.ContainerType
import net.minecraft.inventory.container.Slot
import net.minecraft.network.PacketBuffer

open class ContainerShulkerBox(type: ContainerType<out ContainerShulkerBox>, id: Int, player: EntityPlayer, boxInventory: IInventory) : ContainerBaseChest<IInventory>(type, id, player, boxInventory, boxInventory.size / 9) {
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(ModContainers.SHULKER_BOX, id, inventory.player, Inventory(buffer.readVarInt()))
	constructor(id: Int, inventory: PlayerInventory, boxInventory: IInventory) : this(ModContainers.SHULKER_BOX, id, inventory.player, boxInventory)
	
	override fun wrapChestSlot(slot: Slot): Slot {
		return SlotShulkerBox(slot)
	}
}
