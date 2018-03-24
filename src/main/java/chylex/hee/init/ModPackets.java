package chylex.hee.init;

import chylex.hee.network.IPacket;
import kotlin.Pair;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Supplier;

final class ModPackets{
	/**
	 * Kotlin can't figure out how to use parameterless constructors.
	 * On the other hand, Java can't really figure out the whole generics thing.
	 */
	@SuppressWarnings({"UnusedReturnValue", "unchecked"})
	public static @Nonnull Iterable<Pair<Class<? extends IPacket>, Supplier<IPacket>>> getAllPackets(){
		return Arrays.asList(
		);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends IPacket> Pair packet(Class<T> cls, Supplier<T> constructor){
		return new Pair<>(cls, constructor);
	}
	
	private ModPackets(){}
}
