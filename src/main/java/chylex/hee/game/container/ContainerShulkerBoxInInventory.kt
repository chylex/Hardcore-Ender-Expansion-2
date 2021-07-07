package chylex.hee.game.container

import chylex.hee.game.inventory.util.DetectSlotChangeListener
import chylex.hee.game.inventory.util.getStack
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.init.ModContainers
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketBuffer

class ContainerShulkerBoxInInventory(id: Int, player: PlayerEntity, private val boxInventory: ItemShulkerBoxOverride.Inv) : ContainerShulkerBox(ModContainers.SHULKER_BOX_IN_INVENTORY, id, player, boxInventory) {
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(id, inventory.player, buffer.readVarInt())
	constructor(id: Int, player: PlayerEntity, slot: Int) : this(id, player, ItemShulkerBoxOverride.Inv(player, ItemShulkerBoxOverride.getBoxSize(player.inventory.getStack(slot)), slot))
	
	private val slotChangeListener = DetectSlotChangeListener(this)
	
	@Suppress("ConvertLambdaToReference")
	override fun detectAndSendChanges() {
		slotChangeListener.run { super.detectAndSendChanges() }?.let(boxInventory::validatePlayerItemOnModification)
	}
}
