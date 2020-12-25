package chylex.hee.system.compatibility;
import chylex.hee.commands.arguments.EnumArgument;
import net.minecraft.block.BlockState;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

public final class EraseGenerics{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BlockState withProperty(final BlockState baseState, final IProperty property, final Comparable value){
		return baseState.with(property, value);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BlockState copyProperty(final BlockState baseState, final BlockState originalState, final IProperty property){
		return baseState.with(property, originalState.get(property));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static EnumArgument<?> createEnumArgument(final Class<?> cls){
		if (!cls.isEnum()){
			throw new IllegalArgumentException("cannot create an EnumArgument for class: " + cls.getName());
		}
		
		return new EnumArgument(cls);
	}
	
	public static IPacket<?> buildPacket(final NetworkDirection direction, final Pair<PacketBuffer, Integer> packet, final ResourceLocation channel){
		return direction.buildPacket(packet, channel).getThis();
	}
	
	private EraseGenerics(){}
}
