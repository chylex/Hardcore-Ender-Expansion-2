package chylex.hee.game.block.state;
import net.minecraft.util.IStringSerializable;

public enum PropertyDefault implements IStringSerializable{
	DEFAULT;
	
	@Override public String getName(){
		return "default";
	}
}
