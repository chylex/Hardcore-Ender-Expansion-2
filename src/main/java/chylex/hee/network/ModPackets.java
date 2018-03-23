package chylex.hee.network;
import com.google.common.collect.Lists;
import kotlin.Pair;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

public final class ModPackets{
	/**
	 * Kotlin can't figure out how to use parameterless constructors.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static @Nonnull Iterable<Pair<Class<? extends IPacket>, Supplier<IPacket>>> getAllPackets(){
		return Lists.newArrayList(
		);
	}
	
	private ModPackets(){}
}
