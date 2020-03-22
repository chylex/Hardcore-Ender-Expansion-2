package chylex.hee.system.util.compatibility;
import chylex.hee.game.commands.util.EnumArgument;
import net.minecraft.block.BlockState;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

public final class EraseGenerics{
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static BlockState withProperty(BlockState baseState, IProperty property, Comparable value){
		return baseState.with(property, value);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static BlockState copyProperty(BlockState baseState, BlockState originalState, IProperty property){
		return baseState.with(property, originalState.get(property));
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static EnumArgument<?> createEnumArgument(Class<?> cls){
		if (!cls.isEnum()){
			throw new IllegalArgumentException("cannot create an EnumArgument for class: " + cls.getName());
		}
		
		return new EnumArgument(cls);
	}
	
	public static IPacket<?> buildPacket(NetworkDirection direction, Pair<PacketBuffer, Integer> packet, ResourceLocation channel){
		return direction.buildPacket(packet, channel).getThis();
	}
}
