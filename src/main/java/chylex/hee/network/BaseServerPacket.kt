package chylex.hee.network
import chylex.hee.init.ModNetwork
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.Side.CLIENT
import net.minecraftforge.fml.relauncher.Side.SERVER

abstract class BaseServerPacket : IPacket{
	final override fun handle(side: Side, player: EntityPlayer){
		when(side){
			CLIENT -> throw UnsupportedOperationException("tried handling a server packet on client side: ${this::class.java.simpleName}")
			SERVER -> (player.world as WorldServer).addScheduledTask { handle(player as EntityPlayerMP) }
		}
	}
	
	abstract fun handle(player: EntityPlayerMP)
	
	// External utility functions
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToServer(){
		ModNetwork.sendToServer(this)
	}
}
