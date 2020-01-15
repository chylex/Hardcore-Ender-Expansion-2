package chylex.hee.game.container
import chylex.hee.game.container.util.DetectSlotChangeListener
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.vanilla.ContainerShulkerBox
import chylex.hee.system.migration.vanilla.EntityPlayer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.ContainerType
import net.minecraft.network.PacketBuffer

class ContainerShulkerBoxInInventory(id: Int, player: EntityPlayer, private val boxInventory: ItemShulkerBoxOverride.Inv) : ContainerShulkerBox(id, player.inventory, boxInventory){
	@Suppress("unused")
	constructor(id: Int, inventory: PlayerInventory, buffer: PacketBuffer) : this(id, inventory.player, buffer.readVarInt())
	constructor(id: Int, player: EntityPlayer, slot: Int) : this(id, player, ItemShulkerBoxOverride.Inv(player, slot))
	
	private val slotChangeListener = DetectSlotChangeListener()
	
	override fun getType(): ContainerType<*>{
		return ModContainers.SHULKER_BOX_IN_INVENTORY
	}
	
	override fun detectAndSendChanges(){
		slotChangeListener.restart(listeners){ super.detectAndSendChanges() }?.let(boxInventory::validatePlayerItemOnModification)
	}
}
