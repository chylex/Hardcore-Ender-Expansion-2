package chylex.hee.mixin;
import chylex.hee.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.ChorusFlowerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChorusFlowerBlock.class)
public abstract class HookChorusFlowerSoil{
	@Redirect(
		method = "tick",
		at = @At(value = "FIELD", target = "Lnet/minecraft/block/Blocks;END_STONE:Lnet/minecraft/block/Block;"),
		require = 2
	)
	private Block replaceInTick(){
		return ModBlocks.HUMUS;
	}
	
	@Redirect(
		method = "isValidPosition",
		at = @At(value = "FIELD", target = "Lnet/minecraft/block/Blocks;END_STONE:Lnet/minecraft/block/Block;"),
		require = 1
	)
	private Block replaceInIsValidPosition(){
		return ModBlocks.HUMUS;
	}
}
