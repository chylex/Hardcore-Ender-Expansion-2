package chylex.hee.init.factory;
import chylex.hee.network.IPacket;
import chylex.hee.network.client.PacketClientFX;
import chylex.hee.network.client.PacketClientLaunchInstantly;
import chylex.hee.network.client.PacketClientMoveYourAss;
import chylex.hee.network.client.PacketClientPotionDuration;
import chylex.hee.network.client.PacketClientRotateInstantly;
import chylex.hee.network.client.PacketClientTeleportInstantly;
import chylex.hee.network.client.PacketClientTrinketBreak;
import chylex.hee.network.client.PacketClientUpdateExperience;
import chylex.hee.network.server.PacketServerContainerEvent;
import chylex.hee.network.server.PacketServerOpenInventoryItem;
import chylex.hee.network.server.PacketServerShiftClickTrinket;
import kotlin.Pair;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Supplier;

public final class PacketConstructors{
	@Nonnull
	@SuppressWarnings({"UnusedReturnValue", "unchecked"})
	public static Iterable<Pair<Class<? extends IPacket>, Supplier<IPacket>>> getAll(){
		return Arrays.asList(
			packet(PacketClientFX.class, PacketClientFX::new),
			packet(PacketClientLaunchInstantly.class, PacketClientLaunchInstantly::new),
			packet(PacketClientMoveYourAss.class, PacketClientMoveYourAss::new),
			packet(PacketClientPotionDuration.class, PacketClientPotionDuration::new),
			packet(PacketClientRotateInstantly.class, PacketClientRotateInstantly::new),
			packet(PacketClientTeleportInstantly.class, PacketClientTeleportInstantly::new),
			packet(PacketClientTrinketBreak.class, PacketClientTrinketBreak::new),
			packet(PacketClientUpdateExperience.class, PacketClientUpdateExperience::new),
			
			packet(PacketServerContainerEvent.class, PacketServerContainerEvent::new),
			packet(PacketServerOpenInventoryItem.class, PacketServerOpenInventoryItem::new),
			packet(PacketServerShiftClickTrinket.class, PacketServerShiftClickTrinket::new)
		);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends IPacket> Pair packet(final Class<T> cls, final Supplier<T> constructor){
		return new Pair<>(cls, constructor);
	}
	
	private PacketConstructors(){}
}
