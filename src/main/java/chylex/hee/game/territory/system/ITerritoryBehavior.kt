package chylex.hee.game.territory.system

import net.minecraft.world.server.ServerWorld

interface ITerritoryBehavior {
	/**
	 * If this value matches the current world tick, all players inside the ticker's territory will be sent a new [chylex.hee.network.client.PacketClientTerritoryEnvironment].
	 */
	val resendClientEnvironmentPacketOnWorldTick: Long
		get() = Long.MIN_VALUE
	
	fun tick(world: ServerWorld)
}
