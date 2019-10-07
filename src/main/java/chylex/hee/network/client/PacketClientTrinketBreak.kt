package chylex.hee.network.client
import chylex.hee.client.util.MC
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class PacketClientTrinketBreak() : BaseClientPacket(){
	constructor(target: Entity, item: Item) : this(){
		this.entityId = target.entityId
		this.item = item
	}
	
	private var entityId: Int? = null
	private lateinit var item: Item
	
	override fun write(buffer: ByteBuf) = buffer.use {
		writeInt(entityId!!)
		writeInt(Item.getIdFromItem(item))
	}
	
	override fun read(buffer: ByteBuf) = buffer.use {
		entityId = readInt()
		item = Item.getItemById(readInt())
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		entityId?.let(player.world::getEntityByID)?.let {
			if (it === player){
				MC.entityRenderer.displayItemActivation(ItemStack(item))
			}
			
			(item as? ITrinketItem)?.spawnClientTrinketBreakFX(it)
		}
	}
}
