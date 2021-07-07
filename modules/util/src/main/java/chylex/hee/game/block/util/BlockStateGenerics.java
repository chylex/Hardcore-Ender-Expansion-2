package chylex.hee.game.block.util;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;

public final class BlockStateGenerics {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BlockState withProperty(final BlockState baseState, final Property property, final Comparable value) {
		return baseState.with(property, value);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BlockState copyProperty(final BlockState baseState, final BlockState originalState, final Property property) {
		return baseState.with(property, originalState.get(property));
	}
	
	private BlockStateGenerics() {}
}
