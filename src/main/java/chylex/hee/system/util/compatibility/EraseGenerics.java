package chylex.hee.system.util.compatibility;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

public final class EraseGenerics{
	@SuppressWarnings("unchecked")
	public static IBlockState withProperty(IBlockState baseState, IProperty property, Comparable value){
		return baseState.withProperty(property, value);
	}
}
