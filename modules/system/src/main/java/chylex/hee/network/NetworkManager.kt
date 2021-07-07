package chylex.hee.network

import chylex.hee.HEE
import chylex.hee.game.Environment
import chylex.hee.game.Resource
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.forge.supply
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.RegistryKey
import net.minecraft.world.World
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT
import net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_SERVER
import net.minecraftforge.fml.network.NetworkEvent.ClientCustomPayloadEvent
import net.minecraftforge.fml.network.NetworkEvent.ServerCustomPayloadEvent
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint
import net.minecraftforge.fml.network.event.EventNetworkChannel
import org.apache.commons.lang3.tuple.Pair
import java.util.function.Predicate
import java.util.function.Supplier

object NetworkManager {
	private val CHANNEL = Resource.Custom("net")
	
	@Suppress("ReplacePutWithAssignment")
	fun initialize(packetConstructors: Iterable<kotlin.Pair<Class<out IPacket>, Supplier<IPacket>>>) {
		if (NetworkManager::network.isInitialized) {
			throw UnsupportedOperationException("cannot initialize NetworkManager multiple times")
		}
		
		val checkVersion = Predicate<String> {
			it == HEE.version
		}
		
		network = NetworkRegistry.newEventChannel(CHANNEL, supply(HEE.version), checkVersion, checkVersion)
		network.registerObject(this)
		
		for ((cls, constructor) in packetConstructors) {
			val id = mapPacketIdToConstructor.size.toByte()
			// kotlin indexer boxes the values
			mapPacketIdToConstructor.put(id, constructor)
			mapPacketClassToId.put(cls, id)
		}
	}
	
	// Internal
	
	private const val MISSING_ID: Byte = -1
	
	private lateinit var network: EventNetworkChannel
	
	private val mapPacketIdToConstructor = Byte2ObjectOpenHashMap<Supplier<IPacket>>()
	private val mapPacketClassToId = Object2ByteOpenHashMap<Class<out IPacket>>().apply { defaultReturnValue(MISSING_ID) }
	
	// Packet receiving
	
	@SubscribeEvent
	fun onClientPacket(e: ServerCustomPayloadEvent) {
		val ctx = e.source.get()
		
		readPacket(e.payload).handle(LogicalSide.CLIENT, Environment.getClientSidePlayer()!!)
		ctx.packetHandled = true
	}
	
	@SubscribeEvent
	fun onServerPacket(e: ClientCustomPayloadEvent) {
		val ctx = e.source.get()
		
		readPacket(e.payload).handle(LogicalSide.SERVER, ctx.sender!!)
		ctx.packetHandled = true
	}
	
	// Packet sending
	
	fun sendToServer(packet: BaseServerPacket) {
		PacketDistributor.SERVER.noArg().send(buildPacket(PLAY_TO_SERVER, writePacket(packet)))
	}
	
	fun sendToAll(packet: BaseClientPacket) {
		PacketDistributor.ALL.noArg().send(buildPacket(PLAY_TO_CLIENT, writePacket(packet)))
	}
	
	fun sendToPlayer(packet: BaseClientPacket, player: PlayerEntity) {
		(player as? ServerPlayerEntity)?.let { PacketDistributor.PLAYER.with(supply(it)).send(buildPacket(PLAY_TO_CLIENT, writePacket(packet))) }
	}
	
	fun sendToDimension(packet: BaseClientPacket, dimension: RegistryKey<World>) {
		PacketDistributor.DIMENSION.with(supply(dimension)).send(buildPacket(PLAY_TO_CLIENT, writePacket(packet)))
	}
	
	fun sendToTracking(packet: BaseClientPacket, entity: Entity) {
		PacketDistributor.TRACKING_ENTITY.with(supply(entity)).send(buildPacket(PLAY_TO_CLIENT, writePacket(packet)))
	}
	
	fun sendToAllAround(packet: BaseClientPacket, point: TargetPoint) {
		PacketDistributor.NEAR.with(supply(point)).send(buildPacket(PLAY_TO_CLIENT, writePacket(packet)))
	}
	
	// Packet wrapping
	
	private fun readPacket(packet: PacketBuffer): IPacket {
		val buffer = packet.copy()
		val id = buffer.readByte()
		
		val constructor = mapPacketIdToConstructor[id] ?: throw IllegalArgumentException("unknown packet id: $id")
		return constructor.get().also { it.read(PacketBuffer(buffer.slice())) }
	}
	
	private fun writePacket(packet: IPacket): Pair<PacketBuffer, Int> {
		val id = mapPacketClassToId.getByte(packet::class.java)
		
		require(id != MISSING_ID) { "packet is not registered: ${packet::class.java.simpleName}" }
		
		val buffer = PacketBuffer(Unpooled.buffer())
		buffer.writeByte(id.toInt())
		packet.write(buffer)
		
		return Pair.of(buffer, 0)
	}
	
	private fun buildPacket(direction: NetworkDirection, packet: Pair<PacketBuffer, Int>): net.minecraft.network.IPacket<*>? {
		return direction.buildPacket<net.minecraft.network.IPacket<*>>(packet, CHANNEL).getThis()
	}
}
