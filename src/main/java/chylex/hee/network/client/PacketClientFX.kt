package chylex.hee.network.client
import chylex.hee.game.block.BlockDeathFlowerDecaying
import chylex.hee.game.block.BlockDragonEggOverride
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.entity.item.EntityFallingObsidian
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.item.ItemCompost
import chylex.hee.game.item.ItemScorchingTool
import chylex.hee.game.item.ItemTableLink
import chylex.hee.game.mechanics.table.TableParticleHandler
import chylex.hee.game.world.util.Teleporter
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.Debug
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class PacketClientFX<T : IFxData>() : BaseClientPacket(){
	private companion object{
		private val RAND = Random()
		
		private val HANDLERS = arrayOf(
			Teleporter.FX_TELEPORT,
			TableParticleHandler.FX_PROCESS_PEDESTALS,
			TableParticleHandler.FX_DRAIN_CLUSTER,
			BlockDeathFlowerDecaying.FX_HEAL,
			BlockDragonEggOverride.FX_BREAK,
			TileEntityTablePedestal.FX_ITEM_UPDATE,
			ItemAbstractEnergyUser.FX_CHARGE,
			ItemCompost.FX_USE,
			ItemScorchingTool.FX_BLOCK_BREAK,
			ItemScorchingTool.FX_ENTITY_HIT,
			ItemTableLink.FX_USE,
			EntityFallingObsidian.FX_FALL,
			EntityItemIgneousRock.FX_BLOCK_SMELT,
			EntityItemIgneousRock.FX_ENTITY_BURN
		)
	}
	
	// Instance
	
	constructor(handler: IFxHandler<T>, data: T) : this(){
		this.handler = handler
		this.data = data
	}
	
	private lateinit var handler: IFxHandler<T>
	private lateinit var data: IFxData
	
	private var buffer: ByteBuf? = null
	
	override fun write(buffer: ByteBuf){
		buffer.writeInt(HANDLERS.indexOf(handler))
		data.write(buffer)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun read(buffer: ByteBuf){
		val index = buffer.readInt()
		
		if (index == -1){
			if (Debug.enabled){
				throw IndexOutOfBoundsException("could not find FX handler")
			}
		}
		else{
			this.handler = HANDLERS[index] as IFxHandler<T>
			this.buffer = buffer.slice()
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		buffer?.let { handler.handle(it, player.world, RAND) }
	}
}
