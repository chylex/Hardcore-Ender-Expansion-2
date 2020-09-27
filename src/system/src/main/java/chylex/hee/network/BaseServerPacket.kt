package chylex.hee.network
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityPlayerMP
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.LogicalSide.CLIENT
import net.minecraftforge.fml.LogicalSide.SERVER

abstract class BaseServerPacket : IPacket{
	final override fun handle(side: LogicalSide, player: EntityPlayer){
		when(side){
			CLIENT -> throw UnsupportedOperationException("tried handling a server packet on client side: ${this::class.java.simpleName}")
			SERVER -> (player.world as ServerWorld).server.execute { handle(player as EntityPlayerMP) }
		}
	}
	
	abstract fun handle(player: EntityPlayerMP)
	
	// External utility functions
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToServer(){
		NetworkManager.sendToServer(this)
	}
}
