package chylex.hee.init

import chylex.hee.network.IPacket
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.network.client.PacketClientMoveYourAss
import chylex.hee.network.client.PacketClientPotionDuration
import chylex.hee.network.client.PacketClientRotateInstantly
import chylex.hee.network.client.PacketClientTeleportInstantly
import chylex.hee.network.client.PacketClientTerritoryEnvironment
import chylex.hee.network.client.PacketClientTrinketBreak
import chylex.hee.network.client.PacketClientUpdateExperience
import chylex.hee.network.client.PacketClientWeather
import chylex.hee.network.server.PacketServerContainerEvent
import chylex.hee.network.server.PacketServerOpenInventoryItem
import chylex.hee.network.server.PacketServerShiftClickTrinket
import chylex.hee.system.reflection.ObjectConstructors
import java.util.function.Supplier

object ModPackets {
	val ALL
		get() = listOf(
			build<PacketClientFX<*>>(),
			build<PacketClientLaunchInstantly>(),
			build<PacketClientMoveYourAss>(),
			build<PacketClientPotionDuration>(),
			build<PacketClientRotateInstantly>(),
			build<PacketClientTeleportInstantly>(),
			build<PacketClientTerritoryEnvironment>(),
			build<PacketClientTrinketBreak>(),
			build<PacketClientUpdateExperience>(),
			build<PacketClientWeather>(),
			
			build<PacketServerContainerEvent>(),
			build<PacketServerOpenInventoryItem>(),
			build<PacketServerShiftClickTrinket>()
		)
	
	@Suppress("UNCHECKED_CAST")
	private inline fun <reified T : IPacket> build(): Pair<Class<out T>, Supplier<IPacket>> {
		return T::class.java to (ObjectConstructors.noArgs<T>() as Supplier<IPacket>)
	}
}
