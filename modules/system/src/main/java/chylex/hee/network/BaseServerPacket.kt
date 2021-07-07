package chylex.hee.network

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.LogicalSide.CLIENT
import net.minecraftforge.fml.LogicalSide.SERVER

abstract class BaseServerPacket : IPacket {
	final override fun handle(side: LogicalSide, player: PlayerEntity) {
		when (side) {
			CLIENT -> throw UnsupportedOperationException("tried handling a server packet on client side: ${this::class.java.simpleName}")
			SERVER -> (player.world as ServerWorld).server.execute { handle(player as ServerPlayerEntity) }
		}
	}
	
	abstract fun handle(player: ServerPlayerEntity)
	
	// External utility functions
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToServer() {
		NetworkManager.sendToServer(this)
	}
}
