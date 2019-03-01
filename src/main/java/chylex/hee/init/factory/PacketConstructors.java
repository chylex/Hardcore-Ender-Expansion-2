package chylex.hee.init.factory;
import chylex.hee.network.IPacket;
import chylex.hee.network.client.PacketClientFX;
import chylex.hee.network.client.PacketClientMoveYourAss;
import chylex.hee.network.client.PacketClientTeleportInstantly;
import chylex.hee.network.client.PacketClientTrinketBreak;
import chylex.hee.network.server.PacketServerContainerEvent;
import chylex.hee.network.server.PacketServerOpenGui;
import chylex.hee.network.server.PacketServerShiftClickTrinket;
import kotlin.Pair;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Supplier;

public final class PacketConstructors{
	@SuppressWarnings({"UnusedReturnValue", "unchecked"})
	public static @Nonnull Iterable<Pair<Class<? extends IPacket>, Supplier<IPacket>>> getAll(){
		return Arrays.asList(
			packet(PacketClientFX.class, PacketClientFX::new),
			packet(PacketClientMoveYourAss.class, PacketClientMoveYourAss::new),
			packet(PacketClientTeleportInstantly.class, PacketClientTeleportInstantly::new),
			packet(PacketClientTrinketBreak.class, PacketClientTrinketBreak::new),
			
			packet(PacketServerContainerEvent.class, PacketServerContainerEvent::new),
			packet(PacketServerOpenGui.class, PacketServerOpenGui::new),
			packet(PacketServerShiftClickTrinket.class, PacketServerShiftClickTrinket::new)
		);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends IPacket> Pair packet(Class<T> cls, Supplier<T> constructor){
		return new Pair<>(cls, constructor);
	}
	
	private PacketConstructors(){}
}
