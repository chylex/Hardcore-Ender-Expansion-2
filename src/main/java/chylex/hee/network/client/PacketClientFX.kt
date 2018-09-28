package chylex.hee.network.client
import chylex.hee.game.block.BlockDragonEggOverride
import chylex.hee.game.item.util.Teleporter
import chylex.hee.network.BaseClientPacket
import chylex.hee.system.Debug
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class PacketClientFX() : BaseClientPacket(){
	private companion object{
		private val RAND = Random()
		
		private val HANDLERS = arrayOf(
			Teleporter.FX_TELEPORT,
			BlockDragonEggOverride.FX_BREAK
		)
	}
	
	interface IFXHandler{
		fun handle(buffer: ByteBuf, world: World, rand: Random)
	}
	
	interface IFXData{
		fun write(buffer: ByteBuf)
	}
	
	// Instance
	
	constructor(handler: IFXHandler, data: IFXData) : this(){
		this.handler = handler
		this.data = data
	}
	
	private lateinit var handler: IFXHandler
	private lateinit var data: IFXData
	
	private var buffer: ByteBuf? = null
	
	override fun write(buffer: ByteBuf){
		buffer.writeInt(HANDLERS.indexOf(handler))
		data.write(buffer)
	}
	
	override fun read(buffer: ByteBuf){
		val index = buffer.readInt()
		
		if (index == -1){
			if (Debug.enabled){
				throw IndexOutOfBoundsException("could not find FX handler")
			}
		}
		else{
			this.handler = HANDLERS[index]
			this.buffer = buffer.slice()
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		buffer?.let { handler.handle(it, player.world, RAND) }
	}
}
