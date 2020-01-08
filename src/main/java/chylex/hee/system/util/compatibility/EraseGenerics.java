package chylex.hee.system.util.compatibility;
import net.minecraft.block.BlockState;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

public final class EraseGenerics{
	@SuppressWarnings("unchecked")
	public static BlockState withProperty(BlockState baseState, IProperty property, Comparable value){
		return baseState.with(property, value);
	}
	
	public static IPacket<?> buildPacket(NetworkDirection direction, Pair<PacketBuffer, Integer> packet, ResourceLocation channel){
		return direction.buildPacket(packet, channel).getThis();
	}
}
