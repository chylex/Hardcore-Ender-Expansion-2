package chylex.hee.game.world.territory

import net.minecraft.world.World

interface ITerritoryTicker {
	/**
	 * If this value matches the current world tick, all players inside the ticker's territory will be sent a new [chylex.hee.network.client.PacketClientTerritoryEnvironment].
	 */
	@JvmDefault
	val resendClientEnvironmentPacketOnWorldTick: Long
		get() = Long.MIN_VALUE
	
	fun tick(world: World)
}
