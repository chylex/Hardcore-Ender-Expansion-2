package chylex.hee.game.container

import chylex.hee.game.container.slot.SlotShulkerBox
import chylex.hee.game.inventory.util.size
import chylex.hee.init.ModContainers
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.ContainerType
import net.minecraft.inventory.container.Slot
import net.minecraft.network.PacketBuffer

open class ContainerShulkerBox(type: ContainerType<out ContainerShulkerBox>, id: Int, player: PlayerEntity, boxInventory: IInventory) : AbstractChestContainer<IInventory>(type, id, player, boxInventory, boxInventory.size / 9) {
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(ModContainers.SHULKER_BOX, id, inventory.player, Inventory(buffer.readVarInt()))
	constructor(id: Int, inventory: PlayerInventory, boxInventory: IInventory) : this(ModContainers.SHULKER_BOX, id, inventory.player, boxInventory)
	
	override fun wrapChestSlot(slot: Slot): Slot {
		return SlotShulkerBox(slot)
	}
}
