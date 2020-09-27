package chylex.hee.network.client
import chylex.hee.client.MC
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import chylex.hee.system.serialization.use
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer

class PacketClientTrinketBreak() : BaseClientPacket(){
	constructor(target: Entity, item: Item) : this(){
		this.entityId = target.entityId
		this.item = item
	}
	
	private var entityId: Int? = null
	private lateinit var item: Item
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeInt(entityId!!)
		writeInt(Item.getIdFromItem(item))
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		entityId = readInt()
		item = Item.getItemById(readInt())
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		entityId?.let(player.world::getEntityByID)?.let {
			if (it === player){
				MC.gameRenderer.displayItemActivation(ItemStack(item))
			}
			
			(item as? ITrinketItem)?.spawnClientTrinketBreakFX(it)
		}
	}
}
