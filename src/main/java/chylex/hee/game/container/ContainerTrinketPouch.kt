package chylex.hee.game.container

import chylex.hee.game.container.slot.SlotTrinketItemNoContainers
import chylex.hee.game.inventory.util.DetectSlotChangeListener
import chylex.hee.game.inventory.util.size
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.init.ModContainers
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.wrapper.InvWrapper

class ContainerTrinketPouch(id: Int, player: PlayerEntity, slot: Int) : AbstractCustomInventoryContainer<ItemTrinketPouch.Inv>(ModContainers.TRINKET_POUCH, id, player, ItemTrinketPouch.Inv(player, slot), HEIGHT) {
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(id, inventory.player, buffer.readVarInt())
	
	companion object {
		const val HEIGHT = 132
		const val MAX_SLOTS = 5
	}
	
	private val slotChangeListener = DetectSlotChangeListener(this)
	
	override fun setupSlots() {
		val containerInventoryHandler = InvWrapper(containerInventory)
		val xStart = 80 - 18 * ((containerInventory.size - 1) / 2)
		
		for (slot in 0 until containerInventory.size) {
			addSlot(SlotTrinketItemNoContainers(containerInventoryHandler, slot, xStart + (18 * slot), 18))
		}
	}
	
	@Suppress("ConvertLambdaToReference")
	override fun detectAndSendChanges() {
		slotChangeListener.run { super.detectAndSendChanges() }?.let(containerInventory::validatePlayerItemOnModification)
	}
}
