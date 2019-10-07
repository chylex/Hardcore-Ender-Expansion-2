package chylex.hee.network
import chylex.hee.init.ModNetwork
import chylex.hee.system.migration.forge.Side
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer

abstract class BaseServerPacket : IPacket{
	final override fun handle(side: Side, player: EntityPlayer){
		when(side){
			Side.CLIENT -> throw UnsupportedOperationException("tried handling a server packet on client side: ${this::class.java.simpleName}")
			Side.SERVER -> (player.world as WorldServer).addScheduledTask { handle(player as EntityPlayerMP) }
		}
	}
	
	abstract fun handle(player: EntityPlayerMP)
	
	// External utility functions
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToServer(){
		ModNetwork.sendToServer(this)
	}
}
