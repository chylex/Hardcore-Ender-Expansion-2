package chylex.hee.init.factory;
import chylex.hee.network.IPacket;
import chylex.hee.network.client.PacketClientFX;
import kotlin.Pair;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Supplier;

public final class PacketConstructors{
	@SuppressWarnings({"UnusedReturnValue", "unchecked"})
	public static @Nonnull Iterable<Pair<Class<? extends IPacket>, Supplier<IPacket>>> getAll(){
		return Arrays.asList(
			packet(PacketClientFX.class, PacketClientFX::new)
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
