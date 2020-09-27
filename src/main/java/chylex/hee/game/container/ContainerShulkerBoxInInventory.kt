package chylex.hee.game.container
import chylex.hee.game.inventory.getStack
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketBuffer

class ContainerShulkerBoxInInventory(id: Int, player: EntityPlayer, private val boxInventory: ItemShulkerBoxOverride.Inv) : ContainerShulkerBox(ModContainers.SHULKER_BOX_IN_INVENTORY, id, player, boxInventory){
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(id, inventory.player, buffer.readVarInt())
	constructor(id: Int, player: EntityPlayer, slot: Int) : this(id, player, ItemShulkerBoxOverride.Inv(player, ItemShulkerBoxOverride.getBoxSize(player.inventory.getStack(slot)), slot))
	
	private val slotChangeListener = DetectSlotChangeListener()
	
	override fun detectAndSendChanges(){
		slotChangeListener.restart(listeners){ super.detectAndSendChanges() }?.let(boxInventory::validatePlayerItemOnModification)
	}
}
