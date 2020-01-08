package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.init.factory.PacketConstructors
import chylex.hee.network.BaseClientPacket
import chylex.hee.network.BaseServerPacket
import chylex.hee.network.IPacket
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.EntityPlayerMP
import chylex.hee.system.util.compatibility.EraseGenerics.buildPacket
import chylex.hee.system.util.facades.Resource
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap
import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.fml.LogicalSide
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

object ModNetwork{
	private val CHANNEL = Resource.Custom("net")
	
	@Suppress("ReplacePutWithAssignment")
	fun initialize(){
		if (::network.isInitialized){
			throw UnsupportedOperationException("cannot initialize ModNetwork multiple times")
		}
		
		val checkVersion = Predicate<String>{
			it == HEE.version
		}
		
		network = NetworkRegistry.newEventChannel(CHANNEL, Supplier { HEE.version }, checkVersion, checkVersion)
		network.registerObject(this)
		
		for((cls, constructor) in PacketConstructors.getAll()){
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
	
	// TODO handle lan?
	
	@SubscribeEvent
	fun onClientPacket(e: ServerCustomPayloadEvent){
		val ctx = e.source.get()
		
		readPacket(e.payload).handle(LogicalSide.CLIENT, HEE.proxy.getClientSidePlayer()!!)
		ctx.packetHandled = true
	}
	
	@SubscribeEvent
	fun onServerPacket(e: ClientCustomPayloadEvent){
		val ctx = e.source.get()
		
		readPacket(e.payload).handle(LogicalSide.SERVER, ctx.sender!!) // UPDATE can be null?
		ctx.packetHandled = true
	}
	
	// Packet sending
	
	fun sendToServer(packet: BaseServerPacket){
		PacketDistributor.SERVER.noArg().send(buildPacket(PLAY_TO_SERVER, writePacket(packet), CHANNEL))
	}
	
	fun sendToAll(packet: BaseClientPacket){
		PacketDistributor.ALL.noArg().send(buildPacket(PLAY_TO_CLIENT, writePacket(packet), CHANNEL))
	}
	
	fun sendToPlayer(packet: BaseClientPacket, player: EntityPlayer){
		(player as? EntityPlayerMP)?.let { PacketDistributor.PLAYER.with { it }.send(buildPacket(PLAY_TO_CLIENT, writePacket(packet), CHANNEL)) }
	}
	
	fun sendToDimension(packet: BaseClientPacket, dimension: DimensionType){
		PacketDistributor.DIMENSION.with { dimension }.send(buildPacket(PLAY_TO_CLIENT, writePacket(packet), CHANNEL))
	}
	
	fun sendToTracking(packet: BaseClientPacket, entity: Entity){
		PacketDistributor.TRACKING_ENTITY.with { entity }.send(buildPacket(PLAY_TO_CLIENT, writePacket(packet), CHANNEL))
	}
	
	fun sendToAllAround(packet: BaseClientPacket, point: TargetPoint){
		PacketDistributor.NEAR.with { point }.send(buildPacket(PLAY_TO_CLIENT, writePacket(packet), CHANNEL))
	}
	
	// Packet wrapping
	
	private fun readPacket(packet: PacketBuffer): IPacket{
		val buffer = packet.copy()
		val id = buffer.readByte()
		
		val constructor = mapPacketIdToConstructor[id] ?: throw IllegalArgumentException("unknown packet id: $id")
		return constructor.get().also { it.read(PacketBuffer(buffer.slice())) }
	}
	
	private fun writePacket(packet: IPacket): Pair<PacketBuffer, Int>{
		val id = mapPacketClassToId.getByte(packet::class.java)
		
		require(id != MISSING_ID){ "packet is not registered: ${packet::class.java.simpleName}" }
		
		val buffer = PacketBuffer(Unpooled.buffer())
		buffer.writeByte(id.toInt())
		packet.write(buffer)
		
		return Pair.of(buffer, 0)
	}
}
