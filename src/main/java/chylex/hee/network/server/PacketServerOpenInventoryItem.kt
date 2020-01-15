package chylex.hee.network.server
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.init.ModContainers
import chylex.hee.network.BaseServerPacket
import chylex.hee.system.migration.vanilla.EntityPlayerMP
import chylex.hee.system.util.getStack
import chylex.hee.system.util.use
import net.minecraft.network.PacketBuffer

class PacketServerOpenInventoryItem() : BaseServerPacket(){
	constructor(slot: Int) : this(){
		this.slot = slot
	}
	
	private var slot: Int? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeVarInt(slot!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		slot = readVarInt()
	}
	
	override fun handle(player: EntityPlayerMP){
		val slot = slot!!
		val stack = player.inventory.getStack(slot)
		
		when(stack.item){
			is ItemTrinketPouch       -> ModContainers.open(player, ItemTrinketPouch.ContainerProvider(stack, slot), slot)
			is ItemShulkerBoxOverride -> ModContainers.open(player, ItemShulkerBoxOverride.ContainerProvider(stack, slot), slot)
		}
	}
}
